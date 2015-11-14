/*
 * Copyright 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package babbq.com.searchplace.data;

import java.util.Comparator;

/**
 * A comparator that compares {@link CodeKitchenItem}s based on their {@code weight} attribute.
 */
public class PlaidItemComparator implements Comparator<CodeKitchenItem> {

    @Override
    public int compare(CodeKitchenItem lhs, CodeKitchenItem rhs) {
        return Float.compare(lhs.weight, rhs.weight);
    }
}
