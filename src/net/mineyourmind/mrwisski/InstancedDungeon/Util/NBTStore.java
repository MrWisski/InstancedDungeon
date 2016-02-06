package net.mineyourmind.mrwisski.InstancedDungeon.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import me.dpohvar.powernbt.api.*;
import me.dpohvar.powernbt.nbt.NBTTagCompound;

import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.Vector;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.FloatTag;
import com.sk89q.jnbt.IntArrayTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.jnbt.LongTag;
import com.sk89q.jnbt.NBTConstants;
import com.sk89q.jnbt.NBTUtils;
import com.sk89q.jnbt.ShortTag;

/** A companion class for storing WorldEdit NBT Data - Used in the MCEE schematic format!
 * Also contains helper function for converting between WE NBT and PNBT NBT.
 * 
 * @author MrWisski
 * @version 1.0.0 31 Jan 2016 - Initial code.
 *
 */
public class NBTStore {

	public HashMap<Vector,NBTCompound> store = new HashMap<Vector,NBTCompound>();
	
	public NBTStore(){
	}
	
	public void add(Vector pos, NBTCompound c){
		Log.debug("NBTStore.add("+Util.vToStr(pos)+")");
		store.put(pos, c);
		//Log.debug(NBTStore.toString(c));
	}
	
	public Set<Vector> getKeys(){
		return store.keySet();
	}
	
	public List<NBTCompound> getValues(){
		return (List<NBTCompound>) store.values();
	}
	public NBTCompound get(Vector pos){
		Log.debug("NBTStore.get("+Util.vToStr(pos)+")");
		return store.get(Util.vToStr(pos));
	}

	public void logContents(){
		Log.debug("store contains : " + store.size());
		Set<Vector> k = store.keySet();
		for(Vector s : k){
			NBTCompound c = store.get(s);
		
			Log.debug(s + " : " + NBTStore.toString(c));
		}
	}
	
	/** Converts WorldEdit NBT -> PowerNBT NBT
	 * 
	 * @param l The WorldEdit ListTag to convert.
	 * @return An NBTList created from l
	 */
	public static NBTList toPNBTList(ListTag l){
		Log.debug("toPNBTList");
		List<Tag> lt = l.getValue();
		
		NBTList r = new NBTList();
		int i = 0;
		for(Tag t : lt){
			
			if(t == null || t.getClass() == null){
				Log.debug("toPNBTList encountered a NULL TAG converting a ListTag :(");
				continue;
			}
			Log.debug("List Item #" + i + " : " + t.getClass().getTypeName());
			if(t.getClass().getTypeName() == "com.sk89q.jnbt.CompoundTag"){
			
				r.add(toPNBT((CompoundTag)t));
			} else {
				r.add(t.getValue());
			}
			i++;
		}
		return r;
		
	}
	/** Converts WE NBT -> PowerNBT NBT
	 * 
	 * @param weT - WorldEdit CompoundTag to conver
	 * @return An NBTCompound created from the contents of weT
	 */
	public static NBTCompound toPNBT(CompoundTag weT){
		Log.debug("NBTStore.toPnbt");
		HashMap<String,Object> rmap = new HashMap<String, Object>();
		
		Map<String,Tag> m = weT.getValue();
		
		int max = weT.getValue().size();
		int cur = 0;
		for(Tag T : m.values()){
			cur++;
			Log.debug("Processing tag "+cur+" of " + max);
			switch(NBTUtils.getTypeCode(T.getClass())){
		       case NBTConstants.TYPE_END:
		    	   Log.debug("TYPE_END found at tag "+cur+" of " + max);
		    	   break;
		        case NBTConstants.TYPE_BYTE:
		        	rmap.put(((ByteTag) T).getName(), ((ByteTag) T).getValue());
		        	break;
		        case NBTConstants.TYPE_SHORT:
		        	rmap.put(((ShortTag) T).getName(), ((ShortTag) T).getValue());
		        	break;
		        case NBTConstants.TYPE_INT:
		        	rmap.put(((IntTag) T).getName(), ((IntTag) T).getValue());
		        	break;
		        case NBTConstants.TYPE_LONG:
		        	rmap.put(((LongTag) T).getName(), ((LongTag) T).getValue());
		        	break;
		        case NBTConstants.TYPE_FLOAT:
		        	rmap.put(((FloatTag) T).getName(), ((FloatTag) T).getValue());
		        	break;
		        case NBTConstants.TYPE_DOUBLE:
		        	rmap.put(((DoubleTag) T).getName(), ((DoubleTag) T).getValue());
		        	break;
		        case NBTConstants.TYPE_BYTE_ARRAY:
		        	rmap.put(((ByteArrayTag) T).getName(), ((ByteArrayTag) T).getValue());
		        	break;
		        case NBTConstants.TYPE_STRING:
		        	rmap.put(((StringTag) T).getName(), ((StringTag) T).getValue());
		        	break;
		        case NBTConstants.TYPE_LIST:
		        	rmap.put(((ListTag) T).getName(), NBTStore.toPNBTList((ListTag) T).toArrayList());
		        	break;
		        case NBTConstants.TYPE_COMPOUND:
		        	rmap.put(((CompoundTag) T).getName(), toPNBT((CompoundTag) T).toHashMap());
		        	break;
		        case NBTConstants.TYPE_INT_ARRAY:
		        	rmap.put(((IntArrayTag) T).getName(), ((IntArrayTag) T).getValue());
		        	break;
		        default:
		            Log.error("ToPNBT : Invalid tag type : " + NBTUtils.getTypeName(T.getClass()) + ".");
			}
		}
				
		NBTCompound r = new NBTCompound(rmap);
		return r;
	}
	
	/** Converts the contents of an NBTCompound to a string.
	 * 
	 * @param c 
	 * @return
	 */
	public static String toString(NBTCompound c){
		String ret = "";
		
		Set<String> k = c.keySet();
		
		for(String s : k){
			ret += "Key : " + s + "\n";
			ret += "     Type : " + c.get(s).getClass().getSimpleName() + "\n";
			ret += "     Value : " + c.getString(s) + "\n";
		}
		return ret;
	}
	
	/** Throws the contents of a WE CompoundTag to the log.
	 * 
	 * @param t
	 */
	public static void logWENBT(CompoundTag t){
		NBTCompound n = NBTStore.toPNBT(t);

		Log.debug(NBTStore.toString(n));
		
	}
	
	public static Class getTagTypeFromDataType(Object v){
		if(v.getClass() == byte.class){
			return ByteTag.class;
		} else if(v.getClass() == short.class){
			return ShortTag.class;
		} else if(v.getClass() == int.class){
			return IntTag.class;
		} else if(v.getClass() == long.class){
			return LongTag.class;
		} else if(v.getClass() == float.class){
			return FloatTag.class;
		} else if(v.getClass() == double.class){
			return DoubleTag.class;
		} else if(v.getClass() == String.class){
			return StringTag.class;
		} else if(v.getClass() == byte[].class.getClass()){
			return ByteArrayTag.class;
		} else if(v.getClass() == int[].class){
			return IntArrayTag.class;
		} else if(v.getClass() == Map.class){
			return CompoundTag.class;
		} else if(v.getClass() == List.class){
			return ListTag.class;
		} else {
			Log.debug("getTagTypeFromDataType : Couldn't find a match for class " + v.getClass().getSimpleName());
		}
		
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static Tag toTag(String n, Object v){
		if(v.getClass() == Byte.class){
			return new ByteTag(n,(byte)v);
		} else if(v.getClass() == Short.class){
			return new ShortTag(n,(short)v);
		} else if(v.getClass() == Integer.class){
			return new IntTag(n,(short)v);
		} else if(v.getClass() == Long.class){
			return new LongTag(n,(long)v);
		} else if(v.getClass() == Float.class){
			return new FloatTag(n,(float)v);
		} else if(v.getClass() == Double.class){
			return new DoubleTag(n,(double)v);
		} else if(v.getClass() == String.class){
			return new StringTag(n,(String)v);
		} else if(v.getClass() == Byte[].class.getClass()){
			return new ByteArrayTag(n,(byte[])v);
		} else if(v.getClass() == Integer[].class){
			return new IntArrayTag(n,(int[])v);
		} else if(v.getClass() == Map.class){
			return new CompoundTag(n,(Map<String, Tag>)v);
		} else if(v.getClass() == List.class){
			List<Object> l = (List<Object>)v;
			Class c = l.get(0).getClass();
			List<Tag> tl = new ArrayList<Tag>();
			
			for(Object o : l){
				tl.add(toTag("",o));
			}
			return new ListTag(n, getTagTypeFromDataType(c), tl);
		} else if(v.getClass() == NBTTagCompound.class){
			return NBTStore.toWENBT(n, new NBTCompound((NBTTagCompound)v));
			
		} else {
			Log.debug("toTag : Couldn't find a match for class : " + v.getClass().getTypeName());
		}
		
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static ListTag toWEList(String n, NBTList li){
		Class ty = li.toArrayList().get(0).getClass();
		List<Object> l = li.getHandleList();
		List<Tag> tl = new ArrayList<Tag>();
		
		for(Object o : l){
			tl.add(toTag("",o));
		}
		return new ListTag(n, (Class<? extends Tag>)getTagTypeFromDataType(ty), tl);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ListTag toWEList(String n, List<Object> li){
		Class ty = li.get(0).getClass();
		List<Tag> tl = new ArrayList<Tag>();
		
		for(Object o : li){
			tl.add(toTag("",o));
		}
		return new ListTag(n, (Class<? extends Tag>)getTagTypeFromDataType(ty), tl);
	}
	
	/** Converts a PowerNBT NBT -> WorldEdit NBT
	 * 
	 * @param t the NBTCompound to convert
	 * @return a CompoundTag based on t
	 */
	@SuppressWarnings("unchecked")
	public static CompoundTag toWENBT(String name, NBTCompound t){
		
		HashMap<String, Tag> raw = new HashMap<String, Tag>();
		//raw.put("", new EndTag());
		
		for (Entry<String, Object> e : t.entrySet()) {
			if(e.getValue().getClass() == Byte.class){
				Log.debug("Found a byte!");
				raw.put(e.getKey(), new ByteTag(e.getKey(), (byte)e.getValue()));				
			} else if(e.getValue().getClass() == Short.class){
				Log.debug("Found a short!");
				raw.put(e.getKey(), new ShortTag(e.getKey(), (short)e.getValue()));				
			} else if(e.getValue().getClass() == Integer.class){
				Log.debug("Found an int");
				raw.put(e.getKey(), new IntTag(e.getKey(), (int)e.getValue()));				
			} else if(e.getValue().getClass() == Long.class){
				Log.debug("Found a long");
				raw.put(e.getKey(), new LongTag(e.getKey(), (long)e.getValue()));				
			} else if(e.getValue().getClass() == Float.class){
				Log.debug("Found a Float");
				raw.put(e.getKey(), new FloatTag(e.getKey(), (float)e.getValue()));				
			} else if(e.getValue().getClass() == Double.class){
				Log.debug("Found a double");
				raw.put(e.getKey(), new DoubleTag(e.getKey(), (double)e.getValue()));				
			} else if(e.getValue().getClass() == String.class){
				Log.debug("Found a string");
				raw.put(e.getKey(), new StringTag(e.getKey(), (String)e.getValue()));				
			} else if(e.getValue().getClass() == Byte[].class.getClass()){
				Log.debug("Found a byte array");
				raw.put(e.getKey(), new ByteArrayTag(e.getKey(), (byte[])e.getValue()));
			} else if(e.getValue().getClass() == Integer[].class){
				Log.debug("Found an int array");
				raw.put(e.getKey(), new IntArrayTag(e.getKey(), (int[])e.getValue()));				
			} else if(e.getValue().getClass() == HashMap.class){
				Log.debug("Found a compound");
				raw.put(e.getKey(), toWENBT(name, (NBTCompound)e.getValue()));
			} else if(e.getValue().getClass() == List.class){
				Log.debug("Found a list");
				raw.put(e.getKey(), NBTStore.toWEList(e.getKey(), (List<Object>)e.getValue()));				
			} else if(e.getValue().getClass() == NBTList.class){
				Log.debug("Found an NBTlist - nothing we can do with this :(");
				//raw.put(e.getKey(), ((NBTList)e));				
			} else {
				Log.debug("toWENBT : Couldn't find a match for : " + e.getValue().getClass().getSimpleName());
			}
		}
		
		return new CompoundTag(name, raw);
	}
	
	public static Tag repairTagValue(Object value, Tag t){
		switch(NBTUtils.getTypeCode(t.getClass())){
			case NBTConstants.TYPE_END:
				return t; // Nothing to fix here :D
	        case NBTConstants.TYPE_BYTE:
	        	return new ByteTag(t.getName(),(byte)value);
	        case NBTConstants.TYPE_SHORT:
	        	return new ShortTag(t.getName(),(short)value);
	        case NBTConstants.TYPE_INT:
	        	return new IntTag(t.getName(), (int)value);
	        case NBTConstants.TYPE_LONG:
	        	return new LongTag(t.getName(), (long)value);
	        case NBTConstants.TYPE_FLOAT:
	        	return new FloatTag(t.getName(),(float)value);
	        case NBTConstants.TYPE_DOUBLE:
	        	return new DoubleTag(t.getName(), (double)value);
	        case NBTConstants.TYPE_STRING:
	        	return new StringTag(t.getName(),(String)value);
	        case NBTConstants.TYPE_BYTE_ARRAY:
	        	return new ByteArrayTag(t.getName(),(byte[])value);
	        case NBTConstants.TYPE_INT_ARRAY:
	        	return new IntArrayTag(t.getName(),(int[])value);
	        case NBTConstants.TYPE_LIST:
	        	Log.error("Cannot fix tag of type NBTTAG_LIST!");
	        	return null;
	        	//return new ListTag(t.getName(),null);
	        case NBTConstants.TYPE_COMPOUND:
	        	Log.error("Cannot fix tag of type NBTTAG_COMPOUND!");
	        	return null;
	        default:
	            Log.error("Invalid tag type : " + NBTUtils.getTypeName(t.getClass()) + ".");
	            return null;
		}
	}
}
