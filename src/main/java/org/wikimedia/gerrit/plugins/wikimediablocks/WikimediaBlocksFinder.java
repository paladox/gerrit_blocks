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

import static org.wikimedia.gerrit.plugins.wikimediablocks.WikimediaBlocksConfig.KEY;

import com.google.gerrit.common.data.GroupDescription;
import com.google.gerrit.extensions.restapi.AuthException;
import com.google.gerrit.extensions.restapi.IdString;
import com.google.gerrit.extensions.restapi.ResourceNotFoundException;
import com.google.gerrit.extensions.restapi.TopLevelResource;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.account.GroupMembership;
import com.google.gerrit.server.group.GroupResource;
import com.google.gerrit.server.project.ProjectCache;
import com.google.gerrit.server.restapi.group.GroupsCollection;
import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wikimedia.gerrit.plugins.wikimediablocks.WikimediaBlocksConfig;

public class WikimediaBlocksFinder {
  private static final Logger log = LoggerFactory.getLogger(WikimediaBlocksFinder.class);

  private final ProjectCache projectCache;
  private final GroupsCollection groupsCollection;

  @Inject
  WikimediaBlocksFinder(ProjectCache projectCache, GroupsCollection groupsCollection) {
    this.projectCache = projectCache;
    this.groupsCollection = groupsCollection;
  }

  /**
   * @param type type of blocks (ssh or restapi)
   * @param user identified user
   * @return the true or false
   */
  public Optional<WikimediaBlocksConfig.Blocks> firstMatching(WikimediaBlocksConfig.Type type, IdentifiedUser user) {
    Optional<Map<String, WikimediaBlocksConfig.Blocks>> blocks = getBlocks(type);
    if (blocks.isPresent()) {
      GroupMembership memberShip = user.getEffectiveGroups();
      for (String groupName : blocks.get().keySet()) {
        try {
          GroupResource group =
              groupsCollection.parse(TopLevelResource.INSTANCE, IdString.fromDecoded(groupName));
          Optional<GroupDescription.Internal> maybeInternalGroup = group.asInternalGroup();
          if (!maybeInternalGroup.isPresent()) {
            log.error("Ignoring limits for non-internal group ''{}'' in blocks.config", groupName);
          } else if (memberShip.contains(maybeInternalGroup.get().getGroupUUID())) {
            return Optional.ofNullable(blocks.get().get(groupName));
          }
        } catch (ResourceNotFoundException e) {
          log.error("Ignoring limits for unknown group ''{}'' in blocks.config", groupName);
        } catch (AuthException e) {
          log.error("Ignoring limits for non-visible group ''{}'' in blocks.config", groupName);
        }
      }
    }
    return Optional.empty();
  }

  /**
   * @param type type of rate limit
   * @param groupName name of group to lookup up rate limit for
   * @return rate limit
   */
  public Optional<WikimediaBlocksConfig.Blocks> getBlock(WikimediaBlocksConfig.Type type, String groupName) {
    if (getBlocks(type).isPresent()) {
      return Optional.ofNullable(getBlocks(type).get().get(groupName));
    }
    return Optional.empty();
  }

  /**
   * @param type type of rate limit
   * @return map of rate limits per group name
   */
  private Optional<Map<String, WikimediaBlocksConfig.Blocks>> getBlocks(WikimediaBlocksConfig.Type type) {
    Config cfg = projectCache.getAllProjects().getConfig("blocks.config").get();
    WikimediaBlocksConfig blocksCfg = cfg.get(KEY);
    return blocksCfg.getBlocks(type);
  }
}
