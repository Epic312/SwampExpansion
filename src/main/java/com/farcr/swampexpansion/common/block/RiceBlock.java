package com.farcr.swampexpansion.common.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.farcr.swampexpansion.core.other.SwampExTags;
import com.farcr.swampexpansion.core.registry.SwampExBlocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BushBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.fluid.Fluids;
import net.minecraft.fluid.IFluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class RiceBlock extends BushBlock implements IWaterLoggable, IGrowable {
	protected static final VoxelShape SHAPE_SHORT = Block.makeCuboidShape(3.0D, 0.0D, 3.0D, 13.0D, 4.0D, 13.0D);
	protected static final VoxelShape SHAPE_MEDIUM = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 8.0D, 14.0D);
	protected static final VoxelShape SHAPE_TALL = Block.makeCuboidShape(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 5);

    public RiceBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getDefaultState().with(WATERLOGGED, true).with(AGE, 0));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(AGE, WATERLOGGED);
    }
    
    @Override
    protected boolean isValidGround(BlockState state, IBlockReader worldIn, BlockPos pos) {
        Block block = state.getBlock();
        if (worldIn.getFluidState(pos).getLevel() == 8 && block.isIn(SwampExTags.CATTAIL_PLANTABLE_ON)) return true;
        else if (block.getBlock() == Blocks.FARMLAND) return true;
        return false;
    }

    @SuppressWarnings("deprecation")
	public void placeAt(IWorld worldIn, BlockPos pos, int flags) {
    	Random rand = new Random();
    	int type = rand.nextInt(7);
    	
    	BlockState rice = SwampExBlocks.RICE.get().getDefaultState();
    	BlockState tall_up = SwampExBlocks.TALL_RICE.get().getDefaultState().with(DoubleCattailBlock.HALF, DoubleBlockHalf.UPPER);
    	BlockState tall_down = SwampExBlocks.TALL_RICE.get().getDefaultState().with(DoubleCattailBlock.HALF, DoubleBlockHalf.LOWER);
    	
    	boolean waterlogged = worldIn.hasWater(pos);
    	if (type != 0 || !worldIn.getBlockState(pos.up()).isAir()) {
    		worldIn.setBlockState(pos, rice.with(WATERLOGGED, waterlogged).with(AGE, 3 + rand.nextInt(3)), flags);
    	} else {
    		int age = 6 + rand.nextInt(2);
    		worldIn.setBlockState(pos, tall_down.with(WATERLOGGED, waterlogged).with(DoubleRiceBlock.AGE, age), flags);
			waterlogged = worldIn.hasWater(pos.up());
			worldIn.setBlockState(pos.up(), tall_up.with(WATERLOGGED, waterlogged).with(DoubleRiceBlock.AGE, age), flags);
    	} 
    }
    
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return state.get(AGE) >= 4 ? SHAPE_TALL : state.get(AGE) >= 1 ? SHAPE_MEDIUM : SHAPE_SHORT;
     }
   

    @Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		IFluidState ifluidstate = context.getWorld().getFluidState(context.getPos());
		boolean flag = ifluidstate.isTagged(FluidTags.WATER) && ifluidstate.getLevel() == 8;
		return this.getDefaultState().with(WATERLOGGED, flag);
	}
    
    @SuppressWarnings("deprecation")
	public void grow(ServerWorld worldIn, Random rand, BlockPos pos, BlockState state) {
    	int newAge = state.get(AGE) + MathHelper.nextInt(worldIn.rand, 2, 5);
    	if (newAge > 7) newAge = 7 ;
    	if (newAge <= 5 || !worldIn.getBlockState(pos.up()).isAir()) {
    		worldIn.setBlockState(pos, state.with(AGE, newAge));
    	} else {
    		DoubleRiceBlock doubleplantblock = (DoubleRiceBlock)(SwampExBlocks.TALL_RICE.get());
        	IFluidState ifluidstateUp = worldIn.getFluidState(pos.up());
            if (doubleplantblock.getDefaultState().isValidPosition(worldIn, pos) && (worldIn.isAirBlock(pos.up()) || (Boolean.valueOf(ifluidstateUp.isTagged(FluidTags.WATER) && ifluidstateUp.getLevel() == 8)))) {
               doubleplantblock.placeAt(worldIn, pos, 2, newAge);
            }
    	}
    	
     }
    
    @SuppressWarnings("deprecation")
    @Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random random) {
        super.tick(state, worldIn, pos, random);
        int i = state.get(AGE);
        int chance = worldIn.getBlockState(pos.down()).isFertile(worldIn, pos.down()) ? 10 : 12;
        if ((worldIn.getBlockState(pos.down()).getBlock() == Blocks.FARMLAND || worldIn.getFluidState(pos).getLevel() == 8) && worldIn.getLightSubtracted(pos.up(), 0) >= 9 && net.minecraftforge.common.ForgeHooks.onCropsGrowPre(worldIn, pos, state, random.nextInt(chance) == 0)) {
        	if (i == 5) {
        		DoubleRiceBlock doubleplantblock = (DoubleRiceBlock)(SwampExBlocks.TALL_RICE.get());
                if (doubleplantblock.getDefaultState().isValidPosition(worldIn, pos) && worldIn.isAirBlock(pos.up())) {
                	doubleplantblock.placeAt(worldIn, pos, 2, 6);
                    net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state);
                }
        	} else {
        		worldIn.setBlockState(pos, state.with(AGE, state.get(AGE) + 1));
                net.minecraftforge.common.ForgeHooks.onCropsGrowPost(worldIn, pos, state);
        	}
        }
     }
    
    @Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return false;
	}

	@Override
    public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
    	return this.isValidGround(world.getBlockState(pos.down()), world, pos);
    }
    
    @SuppressWarnings("deprecation")
    @Override
	public IFluidState getFluidState(BlockState state) {
		return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);	
	}

    @Override
    public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (!state.isValidPosition(worldIn, currentPos)) {
			return Blocks.AIR.getDefaultState();
		} else {
			if (state.get(WATERLOGGED)) {
				worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));	
			}
			return super.updatePostPlacement(state, facing, facingState, worldIn, currentPos, facingPos);	
		}	
	}

	@Override
	public boolean canGrow(IBlockReader worldIn, BlockPos pos, BlockState state, boolean isClient) {
		return true;
	}

	@Override
	public boolean canUseBonemeal(World worldIn, Random rand, BlockPos pos, BlockState state) {
		return true;
	}
}