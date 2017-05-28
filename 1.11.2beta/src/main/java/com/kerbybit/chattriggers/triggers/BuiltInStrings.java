package com.kerbybit.chattriggers.triggers;

import com.kerbybit.chattriggers.chat.ChatHandler;
import com.kerbybit.chattriggers.commands.CommandReference;
import com.kerbybit.chattriggers.globalvars.Settings;
import com.kerbybit.chattriggers.globalvars.global;
import com.kerbybit.chattriggers.gui.IconHandler;
import com.kerbybit.chattriggers.objects.ListHandler;
import com.kerbybit.chattriggers.objects.JsonHandler;
import com.kerbybit.chattriggers.references.Reference;
import com.kerbybit.chattriggers.references.RomanNumber;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.Math.floor;
import static java.lang.StrictMath.round;

public class BuiltInStrings {
    public static String builtInStrings(String TMP_e, ClientChatReceivedEvent chatEvent, Boolean isAsync) {
        while (TMP_e.contains("{imported(") && TMP_e.contains(")}")) {
            List<String> temporary = new ArrayList<String>();
            String imp = TMP_e.substring(TMP_e.indexOf("{imported(")+10, TMP_e.indexOf(")}", TMP_e.indexOf("{imported(")));
            temporary.add("DefaultString->IMPORTED"+imp+"-"+(global.TMP_string.size()+1));
            Boolean isImported = false;

            File dir = new File("./mods/ChatTriggers/Imports/");
            if (!dir.exists()) {
                if (!dir.mkdir()) {
                    ChatHandler.warn(ChatHandler.color("red", "Unable to create file!"));}
            }
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        if (file.getName().equals(imp+".txt")) {
                            isImported = true;
                        }
                    }
                }
            }

            if (isImported) {
                temporary.add("true");
            } else {
                temporary.add("false");
            }

            global.TMP_string.add(temporary);
            global.backupTMP_strings.add(temporary);

            TMP_e = TMP_e.replace("{imported("+imp+")}", "{string[DefaultString->IMPORTED"+imp+"-"+global.TMP_string.size()+"]}");
        }
        while (TMP_e.contains("{random(") && TMP_e.contains(")}")) {
            List<String> temporary = new ArrayList<String>();
            String lowhigh = TMP_e.substring(TMP_e.indexOf("{random(")+8, TMP_e.indexOf(")}", TMP_e.indexOf("{random(")));
            temporary.add("DefaultString->RANDOM"+lowhigh+"-"+(global.TMP_string.size()+1));

            try {
                int low = 0;
                int high;
                if (lowhigh.contains(",")) {
                    String[] tmp_lowhigh = lowhigh.split(",");
                    String strlow = StringFunctions.nestedArgs(tmp_lowhigh[0].trim(), chatEvent, isAsync);
                    String strhigh = StringFunctions.nestedArgs(tmp_lowhigh[1].trim(), chatEvent, isAsync);
                    low = Integer.parseInt(strlow);
                    high = Integer.parseInt(strhigh);
                } else {
                    String strhigh = StringFunctions.nestedArgs(lowhigh, chatEvent, isAsync);
                    high = Integer.parseInt(strhigh);
                }

                temporary.add(EventsHandler.randInt(low,high) + "");
            } catch (NumberFormatException e) {
                temporary.add("Not a number!");
            }

            global.TMP_string.add(temporary);
            global.backupTMP_strings.add(temporary);

            TMP_e = TMP_e.replace("{random("+lowhigh+")}", "{string[DefaultString->RANDOM"+lowhigh+"-"+global.TMP_string.size()+"]}");
        }
        while (TMP_e.contains("{msg(") && TMP_e.contains(")}")) {
            String strnum = TMP_e.substring(TMP_e.indexOf("{msg(")+5, TMP_e.indexOf(")}", TMP_e.indexOf("{msg(")));
            List<String> temporary = new ArrayList<String>();
            temporary.add("DefaultString->MSGHISTORY"+strnum+"-"+(global.TMP_string.size()+1));
            String get_number = StringFunctions.nestedArgs(strnum, chatEvent, isAsync);

            try {
                int num = Integer.parseInt(get_number);
                if (num>=0) {
                    if (num<global.chatHistory.size()) {temporary.add(ChatHandler.removeFormatting(global.chatHistory.get(global.chatHistory.size()-(num+1))));}
                    else {temporary.add("Number must be less than the chat history size! ("+global.chatHistory.size()+")");}
                } else {temporary.add("Number must be greater than or equal to 0!");}
            } catch (NumberFormatException e) {temporary.add("Not a number!");}
            global.TMP_string.add(temporary);
            global.backupTMP_strings.add(temporary);

            TMP_e = TMP_e.replace("{msg("+strnum+")}", "{string[DefaultString->MSGHISTORY"+strnum+"-"+global.TMP_string.size()+"]}");
        }
        if (chatEvent!=null) {
            if (TMP_e.contains("{msg}.meta()")) {
                List<String> temporary = new ArrayList<String>();
                temporary.add("DefaultString->MSGMETA-"+(global.TMP_string.size()+1));
                temporary.add(ChatHandler.removeFormatting(chatEvent.getMessage().getStyle().toString()));
                global.TMP_string.add(temporary);
                global.backupTMP_strings.add(temporary);
                TMP_e = TMP_e.replace("{msg}.meta()", "{string[DefaultString->MSGMETA-"+global.TMP_string.size()+"]}");
            }
            if (TMP_e.contains("{msg}")) {
                TMP_e = createDefaultString("msg", ChatHandler.removeFormatting(chatEvent.getMessage().getFormattedText()), TMP_e, isAsync);
            }
        }
        if (TMP_e.contains("{trigsize}")) {
            TMP_e = createDefaultString("trigsize", global.trigger.size()+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{notifysize}")) {
            TMP_e = createDefaultString("notifysize", global.notifySize+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{me}")) {
            TMP_e = createDefaultString("me", Minecraft.getMinecraft().player.getDisplayNameString(), TMP_e, isAsync);
        }
        if (TMP_e.contains("{server}")) {
            String current_server;
            if (Minecraft.getMinecraft().isSingleplayer()) {current_server = "SinglePlayer";}
            else {current_server = Minecraft.getMinecraft().getCurrentServerData().serverName;}

            TMP_e = createDefaultString("server", current_server, TMP_e, isAsync);
        }
        if (TMP_e.contains("{serverMOTD}")) {
            String returnString;
            if (Minecraft.getMinecraft().isSingleplayer()) {returnString = "Single Player world";}
            else {returnString = Minecraft.getMinecraft().getCurrentServerData().serverMOTD;}

            TMP_e = createDefaultString("serverMOTD", returnString, TMP_e, isAsync);
        }
        if (TMP_e.contains("{serverIP}")) {
            String returnString;
            if (Minecraft.getMinecraft().isSingleplayer()) {returnString = "localhost";}
            else {returnString = Minecraft.getMinecraft().getCurrentServerData().serverIP;}

            TMP_e = createDefaultString("serverIP", returnString, TMP_e, isAsync);
        }
        if (TMP_e.contains("{ping}")) {
            String returnString;
            if (Minecraft.getMinecraft().isSingleplayer()) {returnString = "5";}
            else {
                returnString = CommandReference.getPing();
            }

            TMP_e = createDefaultString("ping", returnString, TMP_e, isAsync);
        }
        if (TMP_e.contains("{serverversion}")) {
            String returnString;
            if (Minecraft.getMinecraft().isSingleplayer()) {returnString = "1.8";}
            else {returnString = Minecraft.getMinecraft().getCurrentServerData().gameVersion;}

            TMP_e = createDefaultString("serverversion", returnString, TMP_e, isAsync);
        }
        if (TMP_e.contains("{playerlist}")) {
            StringBuilder returnString = new StringBuilder("[");
            for (EntityPlayer player : Minecraft.getMinecraft().world.playerEntities) {
                returnString.append(player.getName()).append(",");
            }
            if (returnString.toString().equals("[")) {
                System.out.println("wrong one");
                TMP_e = createDefaultString("playerlist", "[]", TMP_e, isAsync);
            } else {
                System.out.println("right one");
                System.out.println(returnString.substring(0, returnString.length()-1)+"]");
                ListHandler.getList("DefaultList->PLAYERLIST-"+(ListHandler.getListsSize()+1), returnString.substring(0, returnString.length()-1)+"]");
                TMP_e = TMP_e.replace("{playerlist}", "{list[DefaultList->PLAYERLIST-"+ListHandler.getListsSize()+"]}");
                System.out.println(ListHandler.getListsSize());
            }
        }
        if (TMP_e.contains("{debug}")) {
            TMP_e = createDefaultString("debug", global.debug + "", TMP_e, isAsync);
        }
        if (TMP_e.contains("{setcol}")) {
            TMP_e = createDefaultString("setcol", Settings.col[0], TMP_e, isAsync);
        }
        if (TMP_e.contains("{br}")) {
            StringBuilder dashes = new StringBuilder();
            float chatWidth = Minecraft.getMinecraft().gameSettings.chatWidth;
            float chatScale = Minecraft.getMinecraft().gameSettings.chatScale;
            int numdash = (int) floor(((((280*(chatWidth))+40)/320) * (1/chatScale))*53);
            for (int j=0; j<numdash; j++) {dashes.append("-");}

            TMP_e = createDefaultString("br", dashes.toString(), TMP_e, isAsync);
        }
        if (TMP_e.contains("{chatwidth}")) {
            TMP_e = createDefaultString("chatwidth", ""+(int)((280*(Minecraft.getMinecraft().gameSettings.chatWidth))+40), TMP_e, isAsync);
        }
        if (TMP_e.contains("{scoreboardtitle}")) {
            String boardTitle = "null";
            try {
                if (Minecraft.getMinecraft().world.getScoreboard().getObjectiveInDisplaySlot(0) != null) {
                    boardTitle = Minecraft.getMinecraft().world.getScoreboard().getObjectiveInDisplaySlot(0).getDisplayName();
                } else if (Minecraft.getMinecraft().world.getScoreboard().getObjectiveInDisplaySlot(1) != null) {
                    boardTitle = Minecraft.getMinecraft().world.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName();
                }
            } catch (Exception e) {
                //Do nothing//
                //catch for ReplayMod//
            }
            TMP_e = createDefaultString("scoreboardtitle", ChatHandler.removeFormatting(boardTitle), TMP_e, isAsync);
        }
        if (TMP_e.contains("{hp}") || TMP_e.contains("{HP}")) {
            TMP_e = createDefaultString("hp", global.playerHealth + "", TMP_e, isAsync);
        }
        if (TMP_e.contains("{sneak}")) {
            TMP_e = createDefaultString("sneak", Minecraft.getMinecraft().player.isSneaking()+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{inchat}")) {
            TMP_e = createDefaultString("inchat", Minecraft.getMinecraft().ingameGUI.getChatGUI().getChatOpen() + "", TMP_e, isAsync);
        }
        if (TMP_e.contains("{coordx}") || TMP_e.contains("{x}")) {
            TMP_e = createDefaultString("coordx", "x", Math.round(Minecraft.getMinecraft().player.posX)+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{coordy}") || TMP_e.contains("{y}")) {
            TMP_e = createDefaultString("coordy", "y", Math.round(Minecraft.getMinecraft().player.posY)+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{coordz}") || TMP_e.contains("{z}")) {
            TMP_e = createDefaultString("coordz", "z", Math.round(Minecraft.getMinecraft().player.posZ)+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{fps}")) {
            TMP_e = createDefaultString("fps", Minecraft.getDebugFPS()+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{fpscol}")) {
            String col;
            if (Minecraft.getDebugFPS() >= global.fpshigh) {
                col = global.fpshighcol;
            } else if (Minecraft.getDebugFPS() >= global.fpslow) {
                col = global.fpsmedcol;
            } else {
                col = global.fpslowcol;
            }

            TMP_e = createDefaultString("fpscol", col, TMP_e, isAsync);
        }
        if (TMP_e.contains("{facing}")) {
            TMP_e = createDefaultString("facing", Minecraft.getMinecraft().player.getHorizontalFacing().toString(), TMP_e, isAsync);
        }
        if (TMP_e.contains("{time}")) {
            Calendar cal = Calendar.getInstance();
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            int minute = cal.get(Calendar.MINUTE);
            String minute_string;
            if (minute < 10) {
                minute_string = "0" + minute;
            } else {
                minute_string = "" + minute;
            }
            if (hour > 12) {
                minute_string = minute_string + "pm";
                hour = hour - 12;
            } else {
                minute_string = minute_string + "am";
            }

            TMP_e = createDefaultString("time", hour + ":" + minute_string, TMP_e, isAsync);
        }
        if (TMP_e.contains("{date}")) {
            DateFormat dateFormat = new SimpleDateFormat(Settings.dateFormat);
            Date date = new Date();
            TMP_e = createDefaultString("date", dateFormat.format(date), TMP_e, isAsync);
        }
        if (TMP_e.contains("{unixtime}")) {
            Date date = new Date();
            TMP_e = createDefaultString("unixtime", date.getTime()+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{potionEffects}")) {
            Collection<PotionEffect> potionEffects = Minecraft.getMinecraft().player.getActivePotionEffects();
            StringBuilder potionList = new StringBuilder("{");
            for (PotionEffect potionEffect : potionEffects) {
                if (!potionEffect.getIsAmbient()) {
                    String potionName = IconHandler.simplifyPotionName(potionEffect.getEffectName().replace("effect.",""));
                    String potionAmp = RomanNumber.toRoman(potionEffect.getAmplifier()+1);
                    int potionDurationInt = potionEffect.getDuration();
                    String potionColor = IconHandler.getPotionColors(potionName);
                    String potionDuration;

                    if (potionEffect.getIsPotionDurationMax()) {
                        potionDuration = "**:**";
                    } else {
                        int minutes = potionDurationInt / 1200;
                        potionDurationInt = potionDurationInt - (minutes * 1200);
                        int seconds = potionDurationInt / 20;
                        if (seconds < 10) {
                            potionDuration = minutes + ":0" + seconds + "";
                        } else {
                            potionDuration = minutes + ":" + seconds + "";
                        }
                    }

                    potionList.append("\"").append(JsonHandler.getForJson(potionName)).append("\":{")
                            .append("\"amplitude\":\"").append(potionAmp)
                            .append("\",\"duration\":\"").append(potionDuration)
                            .append("\",\"color\":\"").append(potionColor).append("\"},");
                }
            }
            if (potionList.toString().equals("{")) {
                potionList.append("}");
            } else {
                potionList = new StringBuilder(potionList.substring(0, potionList.length()-1) + "}");
            }

            JsonHandler.getJson("DefaultJson->POTIONEFFECTS-"+(JsonHandler.getJsonsSize()+1), potionList.toString());

            TMP_e = TMP_e.replace("{potionEffects}", "{json[DefaultJson->POTIONEFFECTS-"+ JsonHandler.getJsonsSize()+"]}");
        }
        if (TMP_e.contains("{armor}")) {
            List<ItemStack> armor_set = Minecraft.getMinecraft().player.inventory.armorInventory;
            StringBuilder armorList = new StringBuilder("{");
            for (int i=armor_set.size()-1; i>=0; i--) {
                ItemStack armor = armor_set.get(i);
                if (armor != null) {
                    float armorMaxDamage = armor.getMaxDamage();
                    float armorDamage = armorMaxDamage - armor.getItemDamage();
                    float armorPercent = (armorDamage / armorMaxDamage) * 100;
                    String armorData = armor.getMetadata()+"";
                    NBTTagCompound armorNBT = armor.getTagCompound();
                    if (armorNBT!=null) {
                        if (armorNBT.hasKey("display")) {
                            String armorNBTbase = armorNBT.getTag("display").toString();
                            String armorColor = null;
                            if (armorNBTbase.startsWith("{color:")) {
                                armorColor = armorNBTbase.substring(7, armorNBTbase.indexOf("}"));
                                if (armorColor.contains(",")) {
                                    armorColor = armorColor.substring(0, armorColor.indexOf(","));
                                }
                            }

                            if (armorData.equals("0") && armorColor!=null) {
                                armorData = "#"+armorColor;
                            }
                        }
                    }
                    armorList.append("\"").append(armor.getItem().getRegistryName().toString().replace("minecraft:","")).append("\":{")
                            .append("\"displayName\":\"").append(JsonHandler.getForJson(armor.getDisplayName()))
                            .append("\",\"maxDurability\":").append((int)floor(armorMaxDamage))
                            .append(",\"durability\":").append((int)floor(armorDamage))
                            .append(",\"durabilityPercent\":").append((int)floor(armorPercent))
                            .append(",\"data\":\"").append(armorData).append("\"},");
                }
            }
            if (armorList.toString().equals("{")) {
                armorList.append("}");
            } else {
                armorList = new StringBuilder(armorList.substring(0, armorList.length()-1) + "}");
            }

            JsonHandler.getJson("DefaultJson->ARMOR-"+(JsonHandler.getJsonsSize()+1), armorList.toString());

            TMP_e = TMP_e.replace("{armor}", "{json[DefaultJson->ARMOR-"+ JsonHandler.getJsonsSize()+"]}");
        }
        if (TMP_e.contains("{heldItem}")) {
            ItemStack item = Minecraft.getMinecraft().player.getHeldItem(EnumHand.MAIN_HAND);
            String held;
            try {
                float itemMaxDamage = item.getMaxDamage();
                float itemDamage = itemMaxDamage - item.getItemDamage();
                float itemPercent = (itemDamage / itemMaxDamage) * 100;
                String itemData = item.getMetadata() + "";
                NBTTagCompound armorNBT = item.getTagCompound();
                if (armorNBT != null) {
                    if (armorNBT.hasKey("display")) {
                        String armorNBTbase = armorNBT.getTag("display").toString();
                        String armorColor = null;
                        if (armorNBTbase.startsWith("{color:")) {
                            armorColor = armorNBTbase.substring(7, armorNBTbase.indexOf("}"));
                            if (armorColor.contains(",")) {
                                armorColor = armorColor.substring(0, armorColor.indexOf(","));
                            }
                        }

                        if (itemData.equals("0") && armorColor != null) {
                            itemData = "#" + armorColor;
                        }
                    }
                }
                held = "{\"" + item.getItem().getRegistryName().toString().replace("minecraft:", "")
                        + "\":{\"displayName\":\"" + JsonHandler.getForJson(item.getDisplayName())
                        + "\",\"maxDurability\":" + (int) floor(itemMaxDamage)
                        + ",\"durability\":" + (int) floor(itemDamage)
                        + ",\"durabilityPercent\":" + (int) floor(itemPercent)
                        + ",\"data\":" + itemData + "}}";
            } catch (Exception e) {
                held = "{}";
            }
            JsonHandler.getJson("DefaultJson->HELDITEM-"+(JsonHandler.getJsonsSize()+1), held);

            TMP_e = TMP_e.replace("{heldItem}", "{json[DefaultJson->HELDITEM-"+ JsonHandler.getJsonsSize()+"]}");
        }
        if (TMP_e.contains("{arrows}")) {
            List<ItemStack> inventory = Minecraft.getMinecraft().player.inventory.mainInventory;
            int arrows = 0;

            for (ItemStack item : inventory) {
                if (item != null) {
                    if (item.getDisplayName().equals("Arrow")) {
                        arrows += item.getCount();
                    }
                }
            }

            TMP_e = createDefaultString("arrows", arrows+"", TMP_e, isAsync);
        }
        if (TMP_e.contains("{cps}")) {
            String returnString;

            returnString = global.clicks.size() + "";

            TMP_e = createDefaultString("cps", returnString.replace(".0",""), TMP_e, isAsync);
        }
        if (TMP_e.contains("{rcps}")) {
            String returnString;

            if (global.rclicks_ave.size() > 0)  {
                returnString = floor(global.rclicks_ave.get(global.rclicks_ave.size()-1)) +"";
            } else {
                returnString = "0";
            }

            TMP_e = createDefaultString("rcps", returnString.replace(".0", ""), TMP_e, isAsync);
        }
        if (TMP_e.contains("{cpsAve}")) {
            String clicksAve;

            if (global.clicks_ave.size() > 0) {
                Double clicks = 0.0;
                for (Double click : global.clicks_ave) {
                    clicks += click;
                }
                clicksAve = round(clicks/global.clicks_ave.size()) + "";
            } else {
                clicksAve = "0";
            }

            TMP_e = createDefaultString("cpsAve", clicksAve.replace(".0",""), TMP_e, isAsync);
        }
        if (TMP_e.contains("{rcpsAve}")) {
            String clicksAve;

            if (global.rclicks_ave.size() > 0) {
                Double clicks = 0.0;
                for (Double click : global.rclicks_ave) {
                    clicks += click;
                }
                clicksAve = clicks/global.rclicks_ave.size() + "";
            } else {
                clicksAve = "0";
            }

            TMP_e = createDefaultString("rcpsAve", clicksAve.replace(".0",""), TMP_e, isAsync);
        }
        if (TMP_e.contains("{cpsMax}")) {
            TMP_e = createDefaultString("cpsMax", global.clicks_max.toString().replace(".0",""), TMP_e, isAsync);
        }
        if (TMP_e.contains("{rcpsMax}")) {
            TMP_e = createDefaultString("rcpsMax", global.rclicks_max.toString().replace(".0",""), TMP_e, isAsync);
        }
        if (TMP_e.contains("{CTVersion}")) {
            TMP_e = createDefaultString("CTVersion", Reference.VERSION, TMP_e, isAsync);
        }

        if (TMP_e.contains("{black}")) {
            TMP_e = createDefaultString("black", "&0", TMP_e, isAsync);
        }
        if (TMP_e.contains("{darkBlue}")) {
            TMP_e = createDefaultString("darkBlue", "&1", TMP_e, isAsync);
        }
        if (TMP_e.contains("{darkGreen}")) {
            TMP_e = createDefaultString("darkGreen", "&2", TMP_e, isAsync);
        }
        if (TMP_e.contains("{darkAqua}")) {
            TMP_e = createDefaultString("darkAqua", "&3", TMP_e, isAsync);
        }
        if (TMP_e.contains("{darkRed}")) {
            TMP_e = createDefaultString("darkRed", "&4", TMP_e, isAsync);
        }
        if (TMP_e.contains("{darkPurple}")) {
            TMP_e = createDefaultString("darkPurple", "&5", TMP_e, isAsync);
        }
        if (TMP_e.contains("{gold}")) {
            TMP_e = createDefaultString("gold", "&6", TMP_e, isAsync);
        }
        if (TMP_e.contains("{gray}")) {
            TMP_e = createDefaultString("gray", "&7", TMP_e, isAsync);
        }
        if (TMP_e.contains("{darkGray}")) {
            TMP_e = createDefaultString("darkGray", "&8", TMP_e, isAsync);
        }
        if (TMP_e.contains("{blue}")) {
            TMP_e = createDefaultString("blue", "&9", TMP_e, isAsync);
        }
        if (TMP_e.contains("{green}")) {
            TMP_e = createDefaultString("green", "&a", TMP_e, isAsync);
        }
        if (TMP_e.contains("{aqua}")) {
            TMP_e = createDefaultString("aqua", "&b", TMP_e, isAsync);
        }
        if (TMP_e.contains("{red}")) {
            TMP_e = createDefaultString("red", "&c", TMP_e, isAsync);
        }
        if (TMP_e.contains("{lightPurple}")) {
            TMP_e = createDefaultString("lightPurple", "&d", TMP_e, isAsync);
        }
        if (TMP_e.contains("{yellow}")) {
            TMP_e = createDefaultString("yellow", "&e", TMP_e, isAsync);
        }
        if (TMP_e.contains("{white}")) {
            TMP_e = createDefaultString("white", "&f", TMP_e, isAsync);
        }
        if (TMP_e.contains("{obfuscated}")) {
            TMP_e = createDefaultString("obfuscated", "&k", TMP_e, isAsync);
        }
        if (TMP_e.contains("{bold}")) {
            TMP_e = createDefaultString("bold", "&l", TMP_e, isAsync);
        }
        if (TMP_e.contains("{strikethrough}")) {
            TMP_e = createDefaultString("strikethrough", "&m", TMP_e, isAsync);
        }
        if (TMP_e.contains("{underline}")) {
            TMP_e = createDefaultString("underline", "&n", TMP_e, isAsync);
        }
        if (TMP_e.contains("{italic}")) {
            TMP_e = createDefaultString("italic", "&o", TMP_e, isAsync);
        }
        if (TMP_e.contains("{reset}")) {
            TMP_e = createDefaultString("reset", "&r", TMP_e, isAsync);
        }
        if (TMP_e.contains("{randcol}")) {
            String col = "&f";
            int num = EventsHandler.randInt(0,15);
            switch(num) {
                case (0):
                case (1):
                case (2):
                case (3):
                case (4):
                case (5):
                case (6):
                case (7):
                case (8):
                case (9):
                    col = "&"+num;
                    break;
                case (10):
                    col = "&a";
                    break;
                case (11):
                    col = "&b";
                    break;
                case (12):
                    col = "&c";
                    break;
                case (13):
                    col = "&d";
                    break;
                case (14):
                    col = "&e";
                    break;
                default:
                    break;
            }
            TMP_e = createDefaultString("randcol", col, TMP_e, isAsync);
        }


        return TMP_e;
    }

    private static String createDefaultString(String string_name, String string_value, String TMP_e, Boolean isAsync) {
        if (isAsync) {
            global.Async_string.put("AsyncDefaultString->" + string_name.toUpperCase() + "-" + (global.Async_string.size() + 1), string_value);
            global.backupAsync_string.put("AsyncDefaultString->" + string_name.toUpperCase() + "-" + global.Async_string.size(), string_value);
            return TMP_e.replace("{" + string_name + "}", "{string[AsyncDefaultString->" + string_name.toUpperCase() + "-" + global.Async_string.size() + "]}");
        } else {
            List<String> temporary = new ArrayList<String>();
            temporary.add("DefaultString->" + string_name.toUpperCase() + "-" + (global.TMP_string.size() + 1));
            temporary.add(string_value);
            global.TMP_string.add(temporary);
            global.backupTMP_strings.add(temporary);
            return TMP_e.replace("{" + string_name + "}", "{string[DefaultString->" + string_name.toUpperCase() + "-" + global.TMP_string.size() + "]}");
        }
    }

    private static String createDefaultString(String string_name1, String string_name2, String string_value, String TMP_e, Boolean isAsync) {
        if (isAsync) {
            global.Async_string.put("AsyncDefaultString->" + string_name1.toUpperCase() + "-" + (global.Async_string.size() + 1), string_value);
            global.backupAsync_string.put("AsyncDefaultString->" + string_name1.toUpperCase() + "-" + global.Async_string.size(), string_value);
            return TMP_e.replace("{" + string_name1 + "}", "{string[AsyncDefaultString->" + string_name1.toUpperCase() + "-" + global.Async_string.size() + "]}")
                    .replace("{" + string_name2 + "}", "{string[AsyncDefaultString->" + string_name1.toUpperCase() + "-" + global.Async_string.size() + "]}");
        } else {
            List<String> temporary = new ArrayList<String>();
            temporary.add("DefaultString->" + string_name1.toUpperCase() + "-" + (global.TMP_string.size() + 1));
            temporary.add(string_value);
            global.TMP_string.add(temporary);
            global.backupTMP_strings.add(temporary);
            return TMP_e.replace("{" + string_name1 + "}", "{string[DefaultString->" + string_name1.toUpperCase() + "-" + global.TMP_string.size() + "]}")
                    .replace("{" + string_name2 + "}", "{string[DefaultString->" + string_name1.toUpperCase() + "-" + global.TMP_string.size() + "]}");
        }
    }
}