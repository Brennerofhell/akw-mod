package ch.danielt.akw.reactor;

import ch.danielt.akw.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Erkennt rechteckige Reaktorhüllen (3–9 Blöcke je Achse) per BFS über die
 * Hüllenblöcke und prüft Hülle und Innenraum. Fehler werden als Liste von
 * {@link ValidationError} mit Positionen gesammelt (max. {@link #MAX_ERRORS}).
 */
public final class ReactorValidator {

    /** Maximale Kantenlänge der Hülle je Achse. */
    public static final int MAX_EDGE = 9;
    /** Obergrenze besuchter Hüllenblöcke während der BFS-Suche (9×9×9). */
    public static final int MAX_VOLUME = MAX_EDGE * MAX_EDGE * MAX_EDGE;
    /** Maximal gesammelte Fehler pro Validierung. */
    public static final int MAX_ERRORS = 8;

    private ReactorValidator() {
    }

    /**
     * Ermittelt die Hüllengrenzen per BFS ab der Controller-Position und validiert
     * die gefundene Box vollständig. Der Controller darf an beliebiger Stelle der
     * Hülle sitzen (Wand, Kante oder Ecke).
     */
    public static Result find(Level level, BlockPos controllerPos) {
        if (!level.getBlockState(controllerPos).is(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get())) {
            return Result.failure(List.of(
                    new ValidationError(ValidationError.Type.GAP, controllerPos)));
        }

        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(controllerPos.immutable());
        queue.add(controllerPos.immutable());
        int minX = controllerPos.getX(), maxX = controllerPos.getX();
        int minY = controllerPos.getY(), maxY = controllerPos.getY();
        int minZ = controllerPos.getZ(), maxZ = controllerPos.getZ();

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (visited.contains(neighbor) || !isShellBlock(level.getBlockState(neighbor))) {
                    continue;
                }
                visited.add(neighbor.immutable());
                minX = Math.min(minX, neighbor.getX());
                maxX = Math.max(maxX, neighbor.getX());
                minY = Math.min(minY, neighbor.getY());
                maxY = Math.max(maxY, neighbor.getY());
                minZ = Math.min(minZ, neighbor.getZ());
                maxZ = Math.max(maxZ, neighbor.getZ());
                if (maxX - minX + 1 > MAX_EDGE || maxY - minY + 1 > MAX_EDGE
                        || maxZ - minZ + 1 > MAX_EDGE || visited.size() > MAX_VOLUME) {
                    return Result.failure(List.of(
                            new ValidationError(ValidationError.Type.TOO_LARGE, controllerPos)));
                }
                queue.addLast(neighbor.immutable());
            }
        }

        return validateBounds(level, controllerPos,
                new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    /**
     * Validiert eine bekannte Hüllen-Box vollständig (Oberfläche + Innenraum).
     * Wird von {@link #find} und von der Tick-Revalidierung mit gespeicherten
     * Grenzen genutzt.
     */
    public static Result validateBounds(Level level, BlockPos controllerPos,
                                        BlockPos min, BlockPos max) {
        List<ValidationError> errors = new ArrayList<>();
        int sizeX = max.getX() - min.getX() + 1;
        int sizeY = max.getY() - min.getY() + 1;
        int sizeZ = max.getZ() - min.getZ() + 1;
        if (sizeX < 3 || sizeY < 3 || sizeZ < 3) {
            return Result.failure(List.of(
                    new ValidationError(ValidationError.Type.GAP, controllerPos)));
        }
        if (sizeX > MAX_EDGE || sizeY > MAX_EDGE || sizeZ > MAX_EDGE) {
            return Result.failure(List.of(
                    new ValidationError(ValidationError.Type.TOO_LARGE, controllerPos)));
        }

        Set<BlockPos> pipes = new HashSet<>();
        Set<BlockPos> cores = new HashSet<>();
        int rods = 0;
        int controllers = 0;
        int energyPorts = 0;
        int itemPorts = 0;

        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int y = min.getY(); y <= max.getY(); y++) {
                for (int z = min.getZ(); z <= max.getZ(); z++) {
                    BlockPos current = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(current);
                    boolean surface = x == min.getX() || x == max.getX()
                            || y == min.getY() || y == max.getY()
                            || z == min.getZ() || z == max.getZ();

                    if (surface) {
                        if (state.is(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get())) {
                            controllers++;
                            if (!current.equals(controllerPos)) {
                                addError(errors, ValidationError.Type.FOREIGN_BLOCK, current);
                            }
                        } else if (state.is(ModBlocks.REACTOR_CASING.get())) {
                            // gültiger Hüllenblock
                        } else if (state.is(ModBlocks.REACTOR_ENERGY_PORT.get())) {
                            energyPorts++;
                        } else if (state.is(ModBlocks.REACTOR_ITEM_PORT.get())) {
                            itemPorts++;
                        } else if (state.isAir()) {
                            addError(errors, ValidationError.Type.GAP, current);
                        } else {
                            addError(errors, ValidationError.Type.FOREIGN_BLOCK, current);
                        }
                        continue;
                    }

                    if (state.isAir() || state.is(ModBlocks.LEAD_BLOCK.get())
                            || state.is(ModBlocks.DAMAGED_REACTOR_CORE.get())) {
                        // Beschädigte Kerne sind inert (zählen nicht als Kern), aber gültig.
                        continue;
                    }
                    if (state.is(ModBlocks.REACTOR_CORE.get())) {
                        cores.add(current);
                    } else if (state.is(ModBlocks.CONTROL_ROD_BLOCK.get())) {
                        rods++;
                    } else if (state.is(ModBlocks.COOLING_PIPE.get())) {
                        pipes.add(current);
                    } else {
                        addError(errors, ValidationError.Type.FOREIGN_BLOCK, current);
                    }
                }
            }
        }

        if (controllers == 0) {
            // Gespeicherte Grenzen passen nicht mehr zur Controller-Position.
            addError(errors, ValidationError.Type.GAP, controllerPos);
        }
        if (cores.isEmpty()) {
            addError(errors, ValidationError.Type.NO_CORE, controllerPos);
        }
        if (energyPorts == 0) {
            addError(errors, ValidationError.Type.NO_ENERGY_PORT, controllerPos);
        }

        Set<BlockPos> connectedPipes = findPipesConnectedToShell(level, pipes, min, max);
        for (BlockPos pipe : pipes) {
            if (!connectedPipes.contains(pipe)) {
                addError(errors, ValidationError.Type.DISCONNECTED_PIPE, pipe);
            }
        }

        if (errors.stream().anyMatch(error -> error.type().blocksAssembly())) {
            return Result.failure(errors);
        }

        int coreNeighbors = 0;
        int coreRodContacts = 0;
        int coreCoolingContacts = 0;
        for (BlockPos core : cores) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = core.relative(direction);
                if (cores.contains(neighbor)) {
                    coreNeighbors++;
                } else if (level.getBlockState(neighbor).is(ModBlocks.CONTROL_ROD_BLOCK.get())) {
                    coreRodContacts++;
                } else if (connectedPipes.contains(neighbor)) {
                    coreCoolingContacts++;
                }
            }
        }

        ReactorLayout layout = new ReactorLayout(
                min.getX() - controllerPos.getX(),
                min.getY() - controllerPos.getY(),
                min.getZ() - controllerPos.getZ(),
                sizeX, sizeY, sizeZ,
                cores.size(), rods, pipes.size(), connectedPipes.size(),
                coreNeighbors, coreRodContacts, coreCoolingContacts,
                energyPorts, itemPorts);
        return new Result(layout, List.copyOf(errors));
    }

    /** Gültige Hüllenblöcke für BFS-Suche und Rohr-Anbindung. */
    private static boolean isShellBlock(BlockState state) {
        return state.is(ModBlocks.REACTOR_CASING.get())
                || state.is(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get())
                || state.is(ModBlocks.REACTOR_ENERGY_PORT.get())
                || state.is(ModBlocks.REACTOR_ITEM_PORT.get());
    }

    private static void addError(List<ValidationError> errors, ValidationError.Type type,
                                 BlockPos pos) {
        if (errors.size() < MAX_ERRORS) {
            errors.add(new ValidationError(type, pos.immutable()));
        }
    }

    private static boolean isOnSurface(BlockPos pos, BlockPos min, BlockPos max) {
        if (pos.getX() < min.getX() || pos.getX() > max.getX()
                || pos.getY() < min.getY() || pos.getY() > max.getY()
                || pos.getZ() < min.getZ() || pos.getZ() > max.getZ()) {
            return false;
        }
        return pos.getX() == min.getX() || pos.getX() == max.getX()
                || pos.getY() == min.getY() || pos.getY() == max.getY()
                || pos.getZ() == min.getZ() || pos.getZ() == max.getZ();
    }

    private static Set<BlockPos> findPipesConnectedToShell(Level level, Set<BlockPos> pipes,
                                                           BlockPos min, BlockPos max) {
        Set<BlockPos> connected = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos pipe : pipes) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pipe.relative(direction);
                if (isOnSurface(neighbor, min, max) && isShellBlock(level.getBlockState(neighbor))) {
                    connected.add(pipe);
                    queue.add(pipe);
                    break;
                }
            }
        }

        while (!queue.isEmpty()) {
            BlockPos current = queue.removeFirst();
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = current.relative(direction);
                if (pipes.contains(neighbor) && connected.add(neighbor)) {
                    queue.addLast(neighbor);
                }
            }
        }
        return connected;
    }

    public record Result(ReactorLayout layout, List<ValidationError> errors) {
        public static Result failure(List<ValidationError> errors) {
            return new Result(ReactorLayout.EMPTY, List.copyOf(errors));
        }

        /** Gültig, wenn kein blockierender Fehler vorliegt (Warnungen sind erlaubt). */
        public boolean valid() {
            return errors.stream().noneMatch(error -> error.type().blocksAssembly());
        }
    }
}
