package ch.danielt.akw.reactor;

import ch.danielt.akw.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Prüft die bestehende zentrierte 3x3x3-, 5x5x5- oder 7x7x7-Hülle und ihren Innenraum. */
public final class ReactorValidator {
    private ReactorValidator() {
    }

    public static Result find(Level level, BlockPos controllerPos, Direction facing) {
        Result mostUsefulError = Result.error(Error.MISSING_CASING);
        for (int size : new int[]{7, 5, 3}) {
            Result result = validate(level, controllerPos, facing, size);
            if (result.valid()) {
                return result;
            }
            if (result.error() != Error.MISSING_CASING) {
                mostUsefulError = result;
            }
        }
        return mostUsefulError;
    }

    public static Result validate(Level level, BlockPos controllerPos, Direction facing, int outerSize) {
        Direction forward = facing.getOpposite();
        Direction right = facing.getClockWise();
        int half = outerSize / 2;
        Set<BlockPos> pipes = new HashSet<>();
        Set<BlockPos> cores = new HashSet<>();
        int rods = 0;

        for (int dx = -half; dx <= half; dx++) {
            for (int dy = -half; dy <= half; dy++) {
                for (int dz = 0; dz < outerSize; dz++) {
                    BlockPos current = controllerPos.relative(right, dx)
                            .relative(Direction.UP, dy).relative(forward, dz);
                    boolean controller = dx == 0 && dy == 0 && dz == 0;
                    boolean shell = Math.abs(dx) == half || Math.abs(dy) == half
                            || dz == 0 || dz == outerSize - 1;
                    BlockState state = level.getBlockState(current);

                    if (shell) {
                        if (controller) {
                            if (!state.is(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get())) {
                                return Result.error(Error.MISSING_CASING);
                            }
                        } else if (!state.is(ModBlocks.REACTOR_CASING.get())) {
                            return Result.error(Error.MISSING_CASING);
                        }
                        continue;
                    }

                    if (state.isAir() || state.is(ModBlocks.LEAD_BLOCK.get())) {
                        continue;
                    }
                    if (state.is(ModBlocks.REACTOR_CORE.get())) {
                        cores.add(current.immutable());
                    } else if (state.is(ModBlocks.CONTROL_ROD_BLOCK.get())) {
                        rods++;
                    } else if (state.is(ModBlocks.COOLING_PIPE.get())) {
                        pipes.add(current.immutable());
                    } else {
                        return Result.error(Error.INVALID_INTERIOR);
                    }
                }
            }
        }

        if (cores.isEmpty()) {
            return Result.error(Error.MISSING_CORE);
        }

        Set<BlockPos> connectedPipes = findPipesConnectedToShell(
                level, pipes, controllerPos, facing, outerSize);
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

        ReactorLayout layout = new ReactorLayout(outerSize, cores.size(), rods, pipes.size(),
                connectedPipes.size(), coreNeighbors, coreRodContacts, coreCoolingContacts);
        return Result.success(layout);
    }

    private static Set<BlockPos> findPipesConnectedToShell(Level level, Set<BlockPos> pipes,
                                                            BlockPos controllerPos, Direction facing,
                                                            int outerSize) {
        Set<BlockPos> connected = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos pipe : pipes) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbor = pipe.relative(direction);
                if (isShellPosition(neighbor, controllerPos, facing, outerSize)
                        && (level.getBlockState(neighbor).is(ModBlocks.REACTOR_CASING.get())
                        || level.getBlockState(neighbor).is(ModBlocks.MULTIBLOCK_REACTOR_CONTROLLER.get()))) {
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

    private static boolean isShellPosition(BlockPos pos, BlockPos controllerPos,
                                           Direction facing, int outerSize) {
        Direction forward = facing.getOpposite();
        Direction right = facing.getClockWise();
        BlockPos delta = pos.subtract(controllerPos);
        int dx = delta.getX() * right.getStepX() + delta.getZ() * right.getStepZ();
        int dy = delta.getY();
        int dz = delta.getX() * forward.getStepX() + delta.getZ() * forward.getStepZ();
        int half = outerSize / 2;
        if (Math.abs(dx) > half || Math.abs(dy) > half || dz < 0 || dz >= outerSize) {
            return false;
        }
        return Math.abs(dx) == half || Math.abs(dy) == half || dz == 0 || dz == outerSize - 1;
    }

    public enum Error {
        NONE("akw.multiblock.assembled"),
        MISSING_CASING("akw.multiblock.error.casing"),
        MISSING_CORE("akw.multiblock.error.core"),
        INVALID_INTERIOR("akw.multiblock.error.interior");

        private final String translationKey;

        Error(String translationKey) {
            this.translationKey = translationKey;
        }

        public String translationKey() {
            return translationKey;
        }
    }

    public record Result(ReactorLayout layout, Error error) {
        public static Result success(ReactorLayout layout) {
            return new Result(layout, Error.NONE);
        }

        public static Result error(Error error) {
            return new Result(ReactorLayout.EMPTY, error);
        }

        public boolean valid() {
            return error == Error.NONE;
        }
    }
}
