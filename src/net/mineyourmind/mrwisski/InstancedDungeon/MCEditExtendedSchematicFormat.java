package net.mineyourmind.mrwisski.InstancedDungeon;

/*
 * WorldEdit
 * Copyright (C) 2012 sk89q <http://www.sk89q.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.bukkit.Material;
import org.bukkit.block.Block;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.NBTConstants;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NBTOutputStream;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.blocks.TileEntityBlock;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.selections.Selection;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;

import me.dpohvar.powernbt.api.NBTCompound;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Log;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.NBTStore;
import net.mineyourmind.mrwisski.InstancedDungeon.Util.Util;

/** MCEditExtendedSchematicFormat - The format of preference for Instanced Dungeon plugin.
 * @author zml2008 - Original (MCEditSchematicFormat)
 * @author MrWisski - Modified to extended, save blocks with Material name, instead of ID.
 * 
 * Please note : If the original NBT data is NOT correctly saved out with the schematic,
 * Things get wierd. If you have a broken WE installation (mismatch between forge ver and WE), you
 * can get around this - which I do in ID, but it requires access to the original data.
 */
public class MCEditExtendedSchematicFormat extends SchematicFormat{
    private static final int MAX_SIZE = Short.MAX_VALUE - Short.MIN_VALUE;

    public MCEditExtendedSchematicFormat() {
        super("MCEditExtended", "mceditex", "mcee");
    }

     @SuppressWarnings("deprecation")
	public NBTStore loadTileEnts(File file) throws IOException, DataException {
    	 Log.debug("Loading TileEnts from file...");
    	 NBTStore ret = new NBTStore();
    	 
         FileInputStream stream = new FileInputStream(file);
         NBTInputStream nbtStream = new NBTInputStream(
                 new GZIPInputStream(stream));

         // Schematic tag
         CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
         nbtStream.close();
         if (!schematicTag.getName().equals("ExtendedSchematic")) {
             throw new DataException("Tag \"ExtendedSchematic\" does not exist or is not first");
         }

         // Check
         Map<String, Tag> schematic = schematicTag.getValue();
         if (!schematic.containsKey("BlockMats")) {
             throw new DataException("Schematic file is missing a \"BlockMats\" tag");
         }

         // Get information
         short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
         short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
         short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

         // Check type of Schematic
         String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
         if (!materials.equals("Alpha_Ex")) {
             throw new DataException("Schematic file is not an Alpha Extended schematic");
         }

         List<Tag> blockMat = getChildTag(schematic,"BlockMats", ListTag.class).getValue();
         byte[] blockData = getChildTag(schematic,"BlockData", ByteArrayTag.class).getValue();

         // Need to pull out tile entities
         List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class)
                 .getValue();
         Map<BlockVector, Map<String, Tag>> tileEntitiesMap =
                 new HashMap<BlockVector, Map<String, Tag>>();
         Log.debug("tileEntities = " + tileEntities.size());
         for (Tag tag : tileEntities) {
             if (!(tag instanceof CompoundTag)) continue;
             CompoundTag t = (CompoundTag) tag;

             int x = 0;
             int y = 0;
             int z = 0;

             Map<String, Tag> values = new HashMap<String, Tag>();

             for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                 if (entry.getKey().equals("x")) {
                     if (entry.getValue() instanceof IntTag) {
                         x = ((IntTag) entry.getValue()).getValue();
                     }
                 } else if (entry.getKey().equals("y")) {
                     if (entry.getValue() instanceof IntTag) {
                         y = ((IntTag) entry.getValue()).getValue();
                     }
                 } else if (entry.getKey().equals("z")) {
                     if (entry.getValue() instanceof IntTag) {
                         z = ((IntTag) entry.getValue()).getValue();
                     }
                 }

                 values.put(entry.getKey(), entry.getValue());
             }

             BlockVector vec = new BlockVector(x, y, z);
             tileEntitiesMap.put(vec, values);
         }

 

         for (int x = 0; x < width; ++x) {
             for (int y = 0; y < height; ++y) {
                 for (int z = 0; z < length; ++z) {
                     int index = y * width * length + z * width + x;
                     BlockVector pt = new BlockVector(x, y, z);
                    // BaseBlock block = getBlockForId(blocks[index], blockData[index]);
                     BaseBlock block = new BaseBlock(Material.getMaterial((String)blockMat.get(index).getValue()).getId());
                     block.setData(blockData[index]);
                     
                     
                     if (block instanceof TileEntityBlock && tileEntitiesMap.containsKey(pt)) {
                         CompoundTag t = new CompoundTag("", tileEntitiesMap.get(pt));
                         Log.debug("Adding tileent at " + Util.vToStr(pt));
                         ret.add((Vector)pt, NBTStore.toPNBT(t));
                         
                     }
  
                 }
             }
         }

         return ret;
     }


    @SuppressWarnings("deprecation")
 	@Override
     public CuboidClipboard load(File file) throws IOException, DataException {
         FileInputStream stream = new FileInputStream(file);
         NBTInputStream nbtStream = new NBTInputStream(
                 new GZIPInputStream(stream));

         Vector origin = new Vector();
         Vector offset = new Vector();

         // Schematic tag
         CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
         nbtStream.close();
         if (!schematicTag.getName().equals("ExtendedSchematic")) {
             throw new DataException("Tag \"ExtendedSchematic\" does not exist or is not first");
         }

         // Check
         Map<String, Tag> schematic = schematicTag.getValue();
         if (!schematic.containsKey("BlockMats")) {
             throw new DataException("Schematic file is missing a \"BlockMats\" tag");
         }

         // Get information
         short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
         short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
         short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

         try {
             int originX = getChildTag(schematic, "WEOriginX", IntTag.class).getValue();
             int originY = getChildTag(schematic, "WEOriginY", IntTag.class).getValue();
             int originZ = getChildTag(schematic, "WEOriginZ", IntTag.class).getValue();
             origin = new Vector(originX, originY, originZ);
         } catch (DataException e) {
             // No origin data
         }

         try {
             int offsetX = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
             int offsetY = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
             int offsetZ = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
             offset = new Vector(offsetX, offsetY, offsetZ);
         } catch (DataException e) {
             // No offset data
         }

         // Check type of Schematic
         String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
         if (!materials.equals("Alpha_Ex")) {
             throw new DataException("Schematic file is not an Alpha Extended schematic");
         }

         //TODO 	
         //		Dear FutureBeard, Convert this to an index of material:data pairs, and just 
         //		save blocks out as references to this index - will cut down on file size tremendously!
         //			-Love, LazyPastBeard ^_^
         
         // Get blocks
         List<Tag> blockMat = getChildTag(schematic,"BlockMats", ListTag.class).getValue();
         byte[] blockData = getChildTag(schematic,"BlockData", ByteArrayTag.class).getValue();
  
//         byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
//         byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
//         byte[] addId = new byte[0];
//         short[] blocks = new short[blockId.length]; // Have to later combine IDs

         // We support 4096 block IDs using the same method as vanilla Minecraft, where
         // the highest 4 bits are stored in a separate byte array.
//         if (schematic.containsKey("AddBlocks")) {
//             addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
//         }

         // Combine the AddBlocks data with the first 8-bit block ID
//         for (int index = 0; index < blockId.length; index++) {
//             if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
//                 blocks[index] = (short) (blockId[index] & 0xFF);
//             } else {
//                 if ((index & 1) == 0) {
//                     blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
//                 } else {
//                     blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
//                 }
//             }
//         }

         // Need to pull out tile entities
         List<Tag> tileEntities = getChildTag(schematic, "TileEntities", ListTag.class)
                 .getValue();
         Map<BlockVector, Map<String, Tag>> tileEntitiesMap =
                 new HashMap<BlockVector, Map<String, Tag>>();

         for (Tag tag : tileEntities) {
             if (!(tag instanceof CompoundTag)) continue;
             CompoundTag t = (CompoundTag) tag;

             int x = 0;
             int y = 0;
             int z = 0;

             Map<String, Tag> values = new HashMap<String, Tag>();

             for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                 if (entry.getKey().equals("x")) {
                     if (entry.getValue() instanceof IntTag) {
                         x = ((IntTag) entry.getValue()).getValue();
                     }
                 } else if (entry.getKey().equals("y")) {
                     if (entry.getValue() instanceof IntTag) {
                         y = ((IntTag) entry.getValue()).getValue();
                     }
                 } else if (entry.getKey().equals("z")) {
                     if (entry.getValue() instanceof IntTag) {
                         z = ((IntTag) entry.getValue()).getValue();
                     }
                 }

                 values.put(entry.getKey(), entry.getValue());
             }

             BlockVector vec = new BlockVector(x, y, z);
             tileEntitiesMap.put(vec, values);
         }

         Vector size = new Vector(width, height, length);
         CuboidClipboard clipboard = new CuboidClipboard(size);
         clipboard.setOrigin(origin);
         clipboard.setOffset(offset);

         for (int x = 0; x < width; ++x) {
             for (int y = 0; y < height; ++y) {
                 for (int z = 0; z < length; ++z) {
                     int index = y * width * length + z * width + x;
                     BlockVector pt = new BlockVector(x, y, z);
                    // BaseBlock block = getBlockForId(blocks[index], blockData[index]);
                     BaseBlock block = new BaseBlock(Material.getMaterial((String)blockMat.get(index).getValue()).getId());
                     block.setData(blockData[index]);
                     
                     
                     if (block instanceof TileEntityBlock && tileEntitiesMap.containsKey(pt)) {
                         ((TileEntityBlock) block).setNbtData(new CompoundTag("", tileEntitiesMap.get(pt)));
                     }
                     clipboard.setBlock(pt, block);
                 }
             }
         }

         return clipboard;
     }

    @SuppressWarnings("deprecation")
	@Override
    public void save(CuboidClipboard clipboard, File file) throws IOException, DataException {
        int width = clipboard.getWidth();
        int height = clipboard.getHeight();
        int length = clipboard.getLength();

        if (width > MAX_SIZE) {
            throw new DataException("Width of region too large for a .schematic");
        }
        if (height > MAX_SIZE) {
            throw new DataException("Height of region too large for a .schematic");
        }
        if (length > MAX_SIZE) {
            throw new DataException("Length of region too large for a .schematic");
        }

        HashMap<String, Tag> schematic = new HashMap<String, Tag>();
        schematic.put("Width", new ShortTag("Width", (short) width));
        schematic.put("Length", new ShortTag("Length", (short) length));
        schematic.put("Height", new ShortTag("Height", (short) height));
        schematic.put("Materials", new StringTag("Materials", "Alpha_Ex"));
        schematic.put("WEOriginX", new IntTag("WEOriginX", clipboard.getOrigin().getBlockX()));
        schematic.put("WEOriginY", new IntTag("WEOriginY", clipboard.getOrigin().getBlockY()));
        schematic.put("WEOriginZ", new IntTag("WEOriginZ", clipboard.getOrigin().getBlockZ()));
        schematic.put("WEOffsetX", new IntTag("WEOffsetX", clipboard.getOffset().getBlockX()));
        schematic.put("WEOffsetY", new IntTag("WEOffsetY", clipboard.getOffset().getBlockY()));
        schematic.put("WEOffsetZ", new IntTag("WEOffsetZ", clipboard.getOffset().getBlockZ()));

        
        //TODO 	
        //		Dear FutureBeard, Convert this to an index of material:data pairs, and just 
        //		save blocks out as references to this index - should cut down on file size tremendously!
        //			-Love, LazyPastBeard ^_^
        
        // Copy
//        byte[] blocks = new byte[width * height * length];
//        byte[] addBlocks = null;

        
        ArrayList<Tag> blockMat = new ArrayList<Tag>(width*height*length);
        blockMat.ensureCapacity(width*height*length);
        for(int x = 0; x < width*height*length;x++){
        	blockMat.add(null);
        }
        byte[] blockData = new byte[width * height * length];
        
        ArrayList<Tag> tileEntities = new ArrayList<Tag>();

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    BaseBlock block = clipboard.getPoint(new BlockVector(x, y, z));

                    // Save 4096 IDs in an AddBlocks section
//                    if (block.getType() > 255) {
//                        if (addBlocks == null) { // Lazily create section
//                            addBlocks = new byte[(blocks.length >> 1) + 1];
//                        }

//                        addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
//                                addBlocks[index >> 1] & 0xF0 | (block.getType() >> 8) & 0xF
//                                : addBlocks[index >> 1] & 0xF | ((block.getType() >> 8) & 0xF) << 4);
//                    }

                    blockMat.set(index, new StringTag(Integer.toString(index), Material.getMaterial(block.getId()).toString()));
                    //blocks[index] = (byte) block.getType();
                    blockData[index] = (byte) block.getData();

                    // Store TileEntity data
                    if (block instanceof TileEntityBlock) {
                        TileEntityBlock tileEntityBlock = block;

                     // Get the list of key/values from the block
                        CompoundTag rawTag = tileEntityBlock.getNbtData();
                        if (rawTag != null) {
                        	//rawTag.getName();
                            Map<String, Tag> values = new HashMap<String, Tag>();
                            for (Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                                values.put(entry.getKey(), entry.getValue());
                            }
                            
                            values.put("id", new StringTag("id", tileEntityBlock.getNbtId()));
                            values.put("x", new IntTag("x", x));
                            values.put("y", new IntTag("y", y));
                            values.put("z", new IntTag("z", z));
                            
                            CompoundTag tileEntityTag = new CompoundTag("TileEntity", values);
                            //Log.debug("Displaying the NBT we got for this tag : ");
                            //NBTStore.logWENBT(tileEntityTag);
                            tileEntities.add(tileEntityTag);
                        }
                    }
                }
            }
        }
        
        schematic.put("BlockMats", new ListTag("BlockMats", StringTag.class, blockMat)); //new ByteArrayTag("Blocks", blocks));
        schematic.put("BlockData", new ByteArrayTag("BlockData", blockData));
        schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
        schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
//        if (addBlocks != null) {
//            schematic.put("AddBlocks", new ByteArrayTag("AddBlocks", addBlocks));
//        }

        // Build and output
        CompoundTag schematicTag = new CompoundTag("ExtendedSchematic", schematic);
        NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(file));
        stream.writeTag(schematicTag);
        stream.close();
    }

    @SuppressWarnings("deprecation")
	public static void saveFromSelection(Selection s, File file) throws IOException, DataException {
    	//Create the clipboard.
    	Vector min = s.getNativeMinimumPoint();
		Vector max = s.getNativeMaximumPoint();

		CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(Vector.ONE), min); 

		//Populate the clipboard.
		BukkitWorld w = new BukkitWorld(s.getWorld());
		EditSession editSession = new EditSession(w, 999999999);
		clipboard.copy(editSession);
		
        int width = clipboard.getWidth();
        int height = clipboard.getHeight();
        int length = clipboard.getLength();

        if (width > MAX_SIZE) {
            throw new DataException("Width of region too large for a .schematic");
        }
        if (height > MAX_SIZE) {
            throw new DataException("Height of region too large for a .schematic");
        }
        if (length > MAX_SIZE) {
            throw new DataException("Length of region too large for a .schematic");
        }

        HashMap<String, Tag> schematic = new HashMap<String, Tag>();
        schematic.put("Width", new ShortTag("Width", (short) width));
        schematic.put("Length", new ShortTag("Length", (short) length));
        schematic.put("Height", new ShortTag("Height", (short) height));
        schematic.put("Materials", new StringTag("Materials", "Alpha_Ex"));
        schematic.put("WEOriginX", new IntTag("WEOriginX", clipboard.getOrigin().getBlockX()));
        schematic.put("WEOriginY", new IntTag("WEOriginY", clipboard.getOrigin().getBlockY()));
        schematic.put("WEOriginZ", new IntTag("WEOriginZ", clipboard.getOrigin().getBlockZ()));
        schematic.put("WEOffsetX", new IntTag("WEOffsetX", clipboard.getOffset().getBlockX()));
        schematic.put("WEOffsetY", new IntTag("WEOffsetY", clipboard.getOffset().getBlockY()));
        schematic.put("WEOffsetZ", new IntTag("WEOffsetZ", clipboard.getOffset().getBlockZ()));
       
        ArrayList<Tag> blockMat = new ArrayList<Tag>(width*height*length);
        blockMat.ensureCapacity(width*height*length);
        for(int x = 0; x < width*height*length;x++){
        	blockMat.add(null);
        }
        byte[] blockData = new byte[width * height * length];
        
        ArrayList<Tag> tileEntities = new ArrayList<Tag>();

        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    BaseBlock block = clipboard.getBlock(new Vector(x, y, z));
                    Block b = s.getWorld().getBlockAt(clipboard.getOrigin().getBlockX() + x, clipboard.getOrigin().getBlockY() + y, clipboard.getOrigin().getBlockZ() + z);
                    
                    blockMat.set(index, new StringTag(Integer.toString(index), Material.getMaterial(block.getId()).toString()));
                    blockData[index] = (byte) block.getData();

                    // Store TileEntity data
                    if (block instanceof TileEntityBlock) {
                    	TileEntityBlock tileEntityBlock = block;
                    	NBTCompound pc = InstancedDungeon.NBTM.read(b);
                    	
                    	//Log.debug(tileEntityBlock.getNbtId());
                    	if(tileEntityBlock.hasNbtData()){
                    		CompoundTag rawTag = null;
                    		if(block.getNbtId() != "Chest"){
                    			// Get the list of key/values from the block
                    			rawTag = NBTStore.toWENBT(tileEntityBlock.getNbtId(),pc);
                    			Log.debug("Dumping the NBT data we got from " + b.getType().toString());
                    			Log.debug(NBTStore.toString(pc));
                    			Log.debug("Dumping the converted NBT Data : ");
                    			NBTStore.logWENBT(rawTag);
                    			// If we're running this - its because WE is broken. We'll pull the
                    			// tag also from PowerNBT, and use that to "correct" the values of all
                    			// the WE tags.
                    		} else {
                    			//We can't handle chests - the read in data is for stupid
                    			//minecraft NBT stuff i dunno how to access yet. Let WE handle it.
                    			
                    			// Get the list of key/values from the block
                                rawTag = tileEntityBlock.getNbtData();
                    		}

                    		if (rawTag != null) {
                    			Map<String, Tag> values = new HashMap<String, Tag>();
                    			for (Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                    				values.put(entry.getKey(), entry.getValue());
                    			}

                    			values.put("id", new StringTag("id", tileEntityBlock.getNbtId()));
                    			values.put("x", new IntTag("x", x));
                    			values.put("y", new IntTag("y", y));
                    			values.put("z", new IntTag("z", z));

                    			CompoundTag tileEntityTag = new CompoundTag("TileEntity", values);
                    			//Log.debug("Displaying the NBT we got for this tag : ");
                    			//NBTStore.logWENBT(tileEntityTag);
                    			tileEntities.add(tileEntityTag);
                    		}
                    	}
                    }
                }
            }
        }
        
        schematic.put("BlockMats", new ListTag("BlockMats", StringTag.class, blockMat)); //new ByteArrayTag("Blocks", blocks));
        schematic.put("BlockData", new ByteArrayTag("BlockData", blockData));
        schematic.put("Entities", new ListTag("Entities", CompoundTag.class, new ArrayList<Tag>()));
        schematic.put("TileEntities", new ListTag("TileEntities", CompoundTag.class, tileEntities));
//        if (addBlocks != null) {
//            schematic.put("AddBlocks", new ByteArrayTag("AddBlocks", addBlocks));
//        }

        // Build and output
        CompoundTag schematicTag = new CompoundTag("ExtendedSchematic", schematic);
        NBTOutputStream stream = new NBTOutputStream(new FileOutputStream(file));
        stream.writeTag(schematicTag);
        stream.close();
    }

    
   @Override
   public boolean isOfFormat(File file) {
       DataInputStream str = null;
       try {
           str = new DataInputStream(new GZIPInputStream(new FileInputStream(file)));
           if ((str.readByte() & 0xFF) != NBTConstants.TYPE_COMPOUND) {
               return false;
           }
           byte[] nameBytes = new byte[str.readShort() & 0xFFFF];
           str.readFully(nameBytes);
           String name = new String(nameBytes, NBTConstants.CHARSET);
           return name.equals("ExtendedSchematic");
       } catch (IOException e) {
           return false;
       } finally {
           if (str != null) {
               try {
                   str.close();
               } catch (IOException ignore) {
                   // blargh
               }
           }
       }
   }

    /**
     * Get child tag of a NBT structure.
     *
     * @param items The parent tag map
     * @param key The name of the tag to get
     * @param expected The expected type of the tag
     * @return child tag casted to the expected type
     * @throws DataException if the tag does not exist or the tag is not of the expected type
     */
    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key,
                                                 Class<T> expected) throws DataException {

        if (!items.containsKey(key)) {
            throw new DataException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new DataException(
                    key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }
}