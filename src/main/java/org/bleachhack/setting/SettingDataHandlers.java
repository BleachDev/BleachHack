package org.bleachhack.setting;

import java.util.Collection;
import java.util.function.Supplier;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class SettingDataHandlers {
	
	public static final SettingDataHandler<Void> NULL = new SettingDataHandler<>() {
		public JsonElement write(Void value) {
			return JsonNull.INSTANCE;
		}

		public Void read(JsonElement json) {
			return null;
		}
	};

	public static final SettingDataHandler<Boolean> BOOLEAN = new SettingDataHandler<>() {
		public JsonElement write(Boolean value) {
			return new JsonPrimitive(value);
		}

		public Boolean read(JsonElement json) {
			return json.getAsBoolean();
		}
	};

	public static final SettingDataHandler<Integer> INTEGER = new SettingDataHandler<>() {
		public JsonElement write(Integer value) {
			return new JsonPrimitive(value);
		}

		public Integer read(JsonElement json) {
			return json.getAsInt();
		}
	};

	public static final SettingDataHandler<Double> DOUBLE = new SettingDataHandler<>() {
		public JsonElement write(Double value) {
			return new JsonPrimitive(value);
		}

		public Double read(JsonElement json) {
			return json.getAsDouble();
		}
	};
	
	public static final SettingDataHandler<float[]> FLOAT_ARRAY = new SettingDataHandler<>() {
		public JsonElement write(float[] value) {
			JsonArray array = new JsonArray();
			for (float f: value)
				array.add(f);
			
			return array;
		}

		public float[] read(JsonElement json) {
			JsonArray array = json.getAsJsonArray();
			float[] farray = new float[array.size()];
			for (int i = 0; i < array.size(); i++)
				farray[i] = array.get(i).getAsFloat();
			
			return farray;
		}
	};

	public static final SettingDataHandler<String> STRING = new SettingDataHandler<>() {
		public JsonElement write(String value) {
			return new JsonPrimitive(value);
		}

		public String read(JsonElement json) {
			return json.getAsString();
		}
	};
	
	public static final SettingDataHandler<Block> BLOCK = new SettingDataHandler<>() {
		public JsonElement write(Block value) {
			return new JsonPrimitive(Registries.BLOCK.getId(value).toString());
		}

		public Block read(JsonElement json) {
			Block bl = Registries.BLOCK.get(new Identifier(json.getAsString()));
			return bl != Blocks.AIR ? bl : null;
		}
	};
	
	public static final SettingDataHandler<Item> ITEM = new SettingDataHandler<>() {
		public JsonElement write(Item value) {
			return new JsonPrimitive(Registries.ITEM.getId(value).toString());
		}

		public Item read(JsonElement json) {
			Item item = Registries.ITEM.get(new Identifier(json.getAsString()));
			return item != Items.AIR ? item : null;
		}
	};
	
	public static <T, C extends Collection<T>> SettingDataHandler<C> ofCollection(SettingDataHandler<T> handler, Supplier<C> supplier) {
		return new SettingDataHandler<C>() {
			public JsonElement write(C values) {
				JsonArray array = new JsonArray();
				for (T val: values)
					array.add(handler.write(val));

				return array;
			}

			public C read(JsonElement json) {
				C collection = supplier.get();
				JsonArray array = json.getAsJsonArray();
				for (JsonElement j: array) {
					collection.add(handler.read(j));
				}

				return collection;
			}
		};
	}
}
