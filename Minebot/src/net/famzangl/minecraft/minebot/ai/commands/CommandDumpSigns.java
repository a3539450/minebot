package net.famzangl.minecraft.minebot.ai.commands;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.common.base.Predicate;

import net.famzangl.minecraft.minebot.ai.AIHelper;
import net.famzangl.minecraft.minebot.ai.command.AIChatController;
import net.famzangl.minecraft.minebot.ai.command.AICommand;
import net.famzangl.minecraft.minebot.ai.command.AICommandInvocation;
import net.famzangl.minecraft.minebot.ai.command.AICommandParameter;
import net.famzangl.minecraft.minebot.ai.command.ParameterType;
import net.famzangl.minecraft.minebot.ai.strategy.AIStrategy;
import net.famzangl.minecraft.minebot.ai.strategy.AIStrategy.TickResult;
import net.famzangl.minecraft.minebot.settings.MinebotSettings;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IChatComponent;

@AICommand(helpText = "Dump all signs to a text file.", name = "minebot")
public class CommandDumpSigns {
	@AICommandInvocation()
	public static AIStrategy run(
			AIHelper helper,
			@AICommandParameter(type = ParameterType.FIXED, fixedName = "dump", description = "") String nameArg,
			@AICommandParameter(type = ParameterType.FIXED, fixedName = "signs", description = "") String nameArg2,
			@AICommandParameter(type = ParameterType.NUMBER, description = "distance", optional = true) Integer distanceArg) {
		final int distance = distanceArg == null ? 100 : distanceArg;
		return new AIStrategy() {
			boolean exported;

			@Override
			protected TickResult onGameTick(AIHelper helper) {
				if (!exported) {
					final File dir = MinebotSettings.getDataDirFile("dumps");
					dir.mkdirs();
					final DateFormat df = new SimpleDateFormat(
							"yyyy-MM-dd-HH-mm-ss");
					final String date = df.format(Calendar.getInstance()
							.getTime());

					final File file = new File(dir, date + ".signdump.txt");
					dumpSignsTo(helper, file, distance);
					exported = true;
				}
				return TickResult.NO_MORE_WORK;
			}

			private void dumpSignsTo(AIHelper helper, File file, int distance) {
				PrintStream out;
				try {
					out = new PrintStream(file);
					int signs = 0;

					for (BlockPos p : helper.findBlocks(Blocks.standing_sign, distance)) {
						dumpAtPos(helper, out, p);
						signs++;
					}
					for (BlockPos p : helper.findBlocks(Blocks.wall_sign, distance)) {
						dumpAtPos(helper, out, p);
						signs++;
					}
					AIChatController.addChatLine("Dumped " + signs + " signs (" + distance + " blocks radius).");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			}

			private void dumpAtPos(AIHelper helper, PrintStream out, BlockPos p) {
				BlockSign block = (BlockSign) helper.getBlock(p);
				TileEntity tileentity = helper.getMinecraft().theWorld
						.getTileEntity(p);
				if (tileentity instanceof TileEntitySign) {
					out.print(p.getX());
					out.print("\t");
					out.print(p.getY());
					out.print("\t");
					out.print(p.getZ());
					IChatComponent[] texts = ((TileEntitySign) tileentity).signText;
					for (IChatComponent t : texts) {
						out.append("\t");
						out.print(t == null ? "" : t.getUnformattedText());
					}
					out.println();
				} else {
					AIChatController.addChatLine("At " + p + ": Not a sign entity.");
				}
			}
		};
	}

}
