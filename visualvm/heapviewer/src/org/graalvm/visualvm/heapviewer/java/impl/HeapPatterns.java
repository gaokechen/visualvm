/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.graalvm.visualvm.heapviewer.java.impl;

import org.graalvm.visualvm.lib.jfluid.heap.Field;
import org.graalvm.visualvm.lib.jfluid.heap.FieldValue;
import org.graalvm.visualvm.lib.jfluid.heap.Instance;
import org.graalvm.visualvm.lib.jfluid.heap.JavaClass;
import org.graalvm.visualvm.lib.jfluid.heap.ObjectFieldValue;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Hurka
 */
@NbBundle.Messages({
    "HeapPatterns_SkippedInstances={0} ({1} instances of {2})"
})
final class HeapPatterns {

    static PathToGCRootPlugin.SkipNode processGCRootReference(ObjectFieldValue reference) {
        ObjectFieldValue ref = reference;
        int skipped = 0;
        for (; ;skipped++) {
            Instance i = ref.getDefiningInstance();
            Instance path = i.getNearestGCRootPointer();
            if (!i.getJavaClass().equals(path.getJavaClass())) {
                break;
            }
            ObjectFieldValue oval = getValueOfField(path, ref.getField());
            if (oval == null || !oval.getInstance().equals(i)) {
                break;
            }
            ref = oval;
        }
        if (skipped>1) {
            JavaClass jcls = ref.getDefiningInstance().getJavaClass();
            String text = Bundle.HeapPatterns_SkippedInstances(ref.getField().getName(), skipped, jcls.getName());
            return new PathToGCRootPlugin.SkipNode(text, ref);
        }
        return null;
    }

    static ObjectFieldValue getValueOfField(Instance i, Field f) {
        for (FieldValue val : i.getFieldValues()) {
            if (val instanceof ObjectFieldValue) {
                ObjectFieldValue oval = (ObjectFieldValue) val;
                if (oval.getField().equals(f)) {
                    return oval;
                }
            }
        }
        return null;
    }
}
