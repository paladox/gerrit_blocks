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

import static com.google.common.base.MoreObjects.firstNonNull;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

import com.google.common.collect.ImmutableList;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.httpd.AllRequestFilter;
import com.google.gerrit.reviewdb.client.Account;
import com.google.gerrit.reviewdb.client.Change;
import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.CurrentUser;
import com.google.gerrit.server.events.CommitReceivedEvent;
import com.google.gerrit.server.events.RefReceivedEvent;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.git.validators.CommitValidationException;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.git.validators.CommitValidationMessage;
import com.google.gerrit.server.git.validators.RefOperationValidationListener;
import com.google.gerrit.server.git.validators.UploadValidationListener;
import com.google.gerrit.server.git.validators.ValidationMessage;
import com.google.gerrit.server.validators.AssigneeValidationListener;
import com.google.gerrit.server.validators.HashtagValidationListener;
import com.google.gerrit.server.validators.ValidationException;
import com.google.gerrit.server.IdentifiedUser;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UploadPack;
import org.wikimedia.gerrit.plugins.wikimediablocks.WikimediaBlocksConfig;

@Singleton
class WikimediaBlocks extends AllRequestFilter
    implements AssigneeValidationListener, CommitValidationListener, HashtagValidationListener, RefOperationValidationListener, UploadValidationListener {

  private static final String MESSAGE_KEY = "message";
  private static final String DEFAULT_MESSAGE = "You have been blocked!";

  private final Provider<CurrentUser> user;
  private final IdentifiedUser.GenericFactory userFactory;
  private final String blockMessage;
  private final WikimediaBlocksFinder finder;

  @Inject
  WikimediaBlocks(
      PluginConfigFactory pluginConfigFactory,
      @PluginName String pluginName,
      Provider<CurrentUser> user,
      IdentifiedUser.GenericFactory userFactory,
      WikimediaBlocksFinder finder) {
    this.user = user;
    this.userFactory = userFactory;
    Config cfg = pluginConfigFactory.getGlobalPluginConfig(pluginName);
    this.blockMessage = firstNonNull(cfg.getString(pluginName, null, MESSAGE_KEY), DEFAULT_MESSAGE);
    this.finder = finder;
  }

  @Override
  public List<CommitValidationMessage> onCommitReceived(CommitReceivedEvent receiveEvent)
      throws CommitValidationException {
    if (getBlockValue()) {
      throw new CommitValidationException(blockMessage);
    }
    return ImmutableList.of();
  }

  @Override
  public List<ValidationMessage> onRefOperation(RefReceivedEvent refEvent) throws ValidationException {
    if (getBlockValue()) {
      throw new ValidationException(blockMessage);
    }
    return ImmutableList.of();
  }

  @Override
  public void onPreUpload(
      Repository repository,
      Project project,
      String remoteHost,
      UploadPack up,
      Collection<? extends ObjectId> wants,
      Collection<? extends ObjectId> haves)
      throws ValidationException {
    if (getBlockValue()) {
      throw new ValidationException(blockMessage);
    }
  }

  @Override
  public void onBeginNegotiate(
      Repository repository,
      Project project,
      String remoteHost,
      UploadPack up,
      Collection<? extends ObjectId> wants,
      int cntOffered)
      throws ValidationException {
    if (getBlockValue()) {
      throw new ValidationException(blockMessage);
    }
  }

  @Override
  public void validateAssignee(Change change, Account assignee) throws ValidationException {
    if (getBlockValue()) {
      throw new ValidationException(blockMessage);
    }
  }

  @Override
  public void validateHashtags(Change change, Set<String> toAdd, Set<String> toRemove)
      throws ValidationException {
    if (getBlockValue()) {
      throw new ValidationException(blockMessage);
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (getBlockValue()
        && request instanceof HttpServletRequest
        && response instanceof HttpServletResponse
        && shouldBlock((HttpServletRequest) request)) {
      ((HttpServletResponse) response).sendError(SC_SERVICE_UNAVAILABLE, blockMessage);
      return;
    }
    chain.doFilter(request, response);
  }

  private boolean shouldBlock(HttpServletRequest request) {
    String method = request.getMethod();
    String servletPath = request.getServletPath();
    return ("POST".equals(method)
            || "GET".equals(method)
            || "PUT".equals(method)
            || "DELETE".equals(method));
  }

  private boolean getBlockValue() {
    CurrentUser u = user.get();
    if (u.isIdentifiedUser()) {
      Account.Id accountId = u.asIdentifiedUser().getAccountId();
      Optional<WikimediaBlocksConfig.Blocks> blocks = finder.firstMatching(WikimediaBlocksConfig.Type.BLOCKS, userFactory.create(accountId));
      if (blocks.isPresent()) {
        return blocks.get().getBlockValue();
      }
    }
    return false;
  }
}
