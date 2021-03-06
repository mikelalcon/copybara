/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.copybara.feedback;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.copybara.DestinationEffect;
import com.google.copybara.Endpoint;
import com.google.copybara.Revision;
import com.google.copybara.SkylarkContext;
import com.google.copybara.transform.SkylarkConsole;
import com.google.devtools.build.lib.skylarkinterface.SkylarkCallable;
import com.google.devtools.build.lib.skylarkinterface.SkylarkModule;
import com.google.devtools.build.lib.skylarkinterface.SkylarkModuleCategory;
import com.google.devtools.build.lib.syntax.SkylarkDict;
import com.google.devtools.build.lib.syntax.SkylarkList;

/**
 * Gives access to the feedback migration information and utilities.
 */
@SkylarkModule(name = "feedback_context",
    category = SkylarkModuleCategory.BUILTIN,
    doc = "Gives access to the feedback migration information and utilities.",
    documented = false)
public class FinishHookContext implements SkylarkContext<FinishHookContext> {

  private final Endpoint origin;
  private final Endpoint destination;
  private final SkylarkRevision resolvedRevision;
  private final SkylarkConsole console;
  private final SkylarkDict params;
  private ImmutableList<DestinationEffect> destinationEffects;

  public FinishHookContext(Endpoint origin, Endpoint destination,
      ImmutableList<DestinationEffect> destinationEffects,
      Revision resolvedRevision, SkylarkConsole console) {
    this(origin, destination, destinationEffects, console, SkylarkDict.empty(),
        new SkylarkRevision(resolvedRevision));
  }

  private FinishHookContext(Endpoint origin, Endpoint destination,
      ImmutableList<DestinationEffect> destinationEffects,
      SkylarkConsole console, SkylarkDict params, SkylarkRevision resolvedRevision) {
    this.origin = Preconditions.checkNotNull(origin);
    this.destination = Preconditions.checkNotNull(destination);
    this.destinationEffects = Preconditions.checkNotNull(destinationEffects);
    this.resolvedRevision = resolvedRevision;
    this.console = Preconditions.checkNotNull(console);
    this.params = Preconditions.checkNotNull(params);
  }

  @SkylarkCallable(name = "origin", doc = "An object representing the origin. Can be used to"
      + " query about the state", structField = true)
  public Endpoint getOrigin() {
    return origin;
  }

  @SkylarkCallable(name = "destination", doc = "An object representing the destination. Can be used"
      + " to query or modify the destination state", structField = true)
  public Endpoint getDestination() {
    return destination;
  }

  @SkylarkCallable(name = "effects",
      doc = "The list of effects that happened in the destination", structField = true)
  public SkylarkList<DestinationEffect> getChanges() {
    return SkylarkList.createImmutable(destinationEffects);
  }

  @SkylarkCallable(name = "revision", doc = "Get the requested/resolved revision",
      structField = true)
  public SkylarkRevision getRevision() {
    return resolvedRevision;
  }

  @SkylarkCallable(name = "console", doc = "Get an instance of the console to report errors or"
      + " warnings", structField = true)
  public SkylarkConsole getConsole() {
    return console;
  }


  @SkylarkCallable(name = "params", doc = "Parameters for the function if created with"
      + " core.dynamic_feedback", structField = true)
  public SkylarkDict getParams() {
    return params;
  }

  @Override
  public FinishHookContext withParams(SkylarkDict<?, ?> params) {
    return new FinishHookContext(origin, destination, destinationEffects, console, params,
                                 resolvedRevision);
  }

  @SkylarkModule(name = "revision_context",
      category = SkylarkModuleCategory.BUILTIN,
      doc = "Information about the revision request/resolved for the migration",
      documented = false)
  private static class SkylarkRevision {

    private final Revision revision;

    SkylarkRevision(Revision revision) {
      this.revision = Preconditions.checkNotNull(revision);
    }

    @SkylarkCallable(name = "labels", doc = "A dictionary with the labels detected for the"
        + " requested/resolved revision.", structField = true)
    public SkylarkDict<String, String> getLabels() {
      return SkylarkDict.copyOf(/* env= */ null, ImmutableMap.copyOf(revision.associatedLabels()));
    }
  }
}
