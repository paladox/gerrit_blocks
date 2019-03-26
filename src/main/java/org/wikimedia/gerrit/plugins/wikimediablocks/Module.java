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

import static com.google.gerrit.server.config.ConfigResource.CONFIG_KIND;

import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.extensions.restapi.RestApiModule;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.git.validators.RefOperationValidationListener;
import com.google.gerrit.server.git.validators.UploadValidationListener;
import com.google.gerrit.server.validators.AssigneeValidationListener;
import com.google.gerrit.server.validators.HashtagValidationListener;
import com.google.inject.AbstractModule;

class Module extends AbstractModule {
  @Override
  protected void configure() {
    DynamicSet.bind(binder(), CommitValidationListener.class).to(WikimediaBlocks.class);
    DynamicSet.bind(binder(), RefOperationValidationListener.class).to(WikimediaBlocks.class);
    DynamicSet.bind(binder(), UploadValidationListener.class).to(WikimediaBlocks.class);
    DynamicSet.bind(binder(), AssigneeValidationListener.class).to(WikimediaBlocks.class);
    DynamicSet.bind(binder(), HashtagValidationListener.class).to(WikimediaBlocks.class);
  }
}
