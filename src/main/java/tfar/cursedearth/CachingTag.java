package tfar.cursedearth;

import net.minecraft.block.Block;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.Optional;

public class CachingTag extends Tag<Block> {
	private static int latestVersion;
	private static TagContainer<Block> container = new TagContainer<>((identifier) -> {
		return Optional.empty();
	}, "", false, "");
		private int version = -1;
		private Tag<Block> delegate;

		public CachingTag(Identifier identifier) {
			super(identifier);
		}

		public boolean contains(Block block) {
			if (this.version != latestVersion) {
				this.delegate = container.getOrCreate(this.getId());
				this.version = latestVersion;
			}

			return this.delegate.contains(block);
		}

		public Collection<Block> values() {
			if (this.version != latestVersion) {
				this.delegate = container.getOrCreate(this.getId());
				this.version = latestVersion;
			}

			return this.delegate.values();
		}

		public Collection<Entry<Block>> entries() {
			if (this.version != latestVersion) {
				this.delegate = container.getOrCreate(this.getId());
				this.version = latestVersion;
			}

			return this.delegate.entries();
		}
	}
