/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package net.elasticgrid.rackspace.cloudservers;

import net.elasticgrid.rackspace.cloudservers.internal.*;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @author Jerome Bernard
 */
public class Personality implements Serializable {
    private final List<File> files;

    public Personality(List<File> files) {
        this.files = files;
    }

    public Personality(final net.elasticgrid.rackspace.cloudservers.internal.Personality personality) {
        this(personalitiesAsFileList(personality));
    }

    private static List<File> personalitiesAsFileList(final net.elasticgrid.rackspace.cloudservers.internal.Personality personality) {
        if (personality == null || personality.getPersonalities() == null)
            return Collections.emptyList();
        List<File> files = new ArrayList<File>(personality.getPersonalities().size());
        for (net.elasticgrid.rackspace.cloudservers.internal.File file : personality.getPersonalities()) {
            files.add(new File(file.getPath(), file.getBase64Binary()));
        }
        return files;
    }

    public List<File> getFiles() {
        return files;
    }
}
