// Copyright (C) 2019 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.wikimedia.gerrit.plugins.wikimediablocks;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Config.ConfigEnum;
import org.eclipse.jgit.lib.Config.SectionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WikimediaBlocksConfig {
  private static final Logger log = LoggerFactory.getLogger(WikimediaBlocksConfig.class);

  static final String BLOCK_GROUP_SECTION = "block";
  static final SectionParser<WikimediaBlocksConfig> KEY =
      new SectionParser<WikimediaBlocksConfig>() {
        @Override
        public WikimediaBlocksConfig parse(final Config cfg) {
          return new WikimediaBlocksConfig(cfg);
        }
      };

  public static class Blocks {
    public Type getType() {
      return type;
    }

    public Boolean getBlockValue() {
      return block;
    }

    private Type type;
    private Boolean block;

    public Blocks(Type type, Boolean block) {
      this.type = type;
      this.block = block;
    }
  }

  public static enum Type implements ConfigEnum {
    BLOCKS;

    @Override
    public String toConfigValue() {
      return name().toLowerCase(Locale.ROOT);
    }

    @Override
    public boolean matchConfigValue(String in) {
      return name().equalsIgnoreCase(in);
    }
  }

  private Table<Type, String, WikimediaBlocksConfig.Blocks> blocks;

  private WikimediaBlocksConfig(final Config c) {
    Set<String> groups = c.getSubsections(BLOCK_GROUP_SECTION);
    if (groups.size() == 0) {
      return;
    }
    blocks = ArrayTable.create(Arrays.asList(WikimediaBlocksConfig.Type.values()), groups);
    for (String groupName : groups) {
      parseBlocks(c, groupName, WikimediaBlocksConfig.Type.BLOCKS);
    }
  }

  void parseBlocks(Config c, String groupName, Type type) {
    String name = type.toConfigValue();
    String value = c.getString(BLOCK_GROUP_SECTION, groupName, name);
    if (value == null) {
      return;
    }

    blocks.put(type, groupName, new WikimediaBlocksConfig.Blocks(type, Boolean.parseBoolean(value.trim())));
  }

  private static boolean match(final String a, final String... cases) {
    for (final String b : cases) {
      if (b != null && b.equalsIgnoreCase(a)) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param type type of rate limit
   * @return map of blocks per group name
   */
  Optional<Map<String, WikimediaBlocksConfig.Blocks>> getBlocks(Type type) {
    if (blocks != null) {
      return Optional.ofNullable(blocks.row(type));
    }
    return Optional.empty();
  }
}
