package com.farcr.swampexpansion.common.world.gen.feature;

import com.farcr.swampexpansion.common.block.CattailBlock;
import com.farcr.swampexpansion.core.registry.SwampExBlocks;
import com.mojang.datafixers.Dynamic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import java.util.Random;
import java.util.function.Function;

public class CattailsFeature extends Feature<NoFeatureConfig> {
    public CattailsFeature(Function<Dynamic<?>, ? extends NoFeatureConfig> config) {
        super(config);
    }

    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random random, BlockPos pos, NoFeatureConfig config) {
        boolean place = false;
        for(int i = 0; i < 512; ++i) {
            BlockPos placePos = pos.add(random.nextInt(8) - random.nextInt(4), random.nextInt(4) - random.nextInt(4), random.nextInt(8) - random.nextInt(4));
            if (i == 0 && !world.hasWater(placePos)){
            	return false;
            }
            if ((world.hasWater(placePos) || world.isAirBlock(placePos)) && world.isAirBlock(placePos.up()) && placePos.getY() < world.getWorld().getDimension().getHeight() - 2 && SwampExBlocks.CATTAIL.get().getDefaultState().isValidPosition(world, placePos)) {
            	((CattailBlock) SwampExBlocks.CATTAIL.get()).placeAt(world, placePos, 2);
            	place = true;
            }
        }
        return place;
    }   
}