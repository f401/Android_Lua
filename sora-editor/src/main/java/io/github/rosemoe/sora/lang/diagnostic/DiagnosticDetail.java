/*******************************************************************************
 *    sora-editor - the awesome code editor for Android
 *    https://github.com/Rosemoe/sora-editor
 *    Copyright (C) 2020-2024  Rosemoe
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 *
 *     Please contact Rosemoe by email 2073412493@qq.com if you need
 *     additional information or have any questions
 ******************************************************************************/

package io.github.rosemoe.sora.lang.diagnostic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Objects;

import java.util.List;

public class DiagnosticDetail {
    @NonNull
    private final CharSequence briefMessage;
    @Nullable
    private final CharSequence detailedMessage;
    @Nullable
    private final List<Quickfix> quickfixes;
    @Nullable
    private final Object extraData;

    public DiagnosticDetail(@NonNull CharSequence briefMessage, @Nullable CharSequence detailedMessage, @Nullable List<Quickfix> quickfixes, @Nullable Object extraData) {
        this.briefMessage = briefMessage;
        this.detailedMessage = detailedMessage;
        this.quickfixes = quickfixes;
        this.extraData = extraData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DiagnosticDetail)) return false;
        DiagnosticDetail that = (DiagnosticDetail) o;
        return Objects.equal(briefMessage, that.briefMessage) && Objects.equal(detailedMessage, that.detailedMessage) && Objects.equal(quickfixes, that.quickfixes) && Objects.equal(extraData, that.extraData);
    }

    @NonNull
    @Override
    public String toString() {
        return "DiagnosticDetail{" +
                "briefMessage=" + briefMessage +
                ", detailedMessage=" + detailedMessage +
                ", quickfixes=" + quickfixes +
                ", extraData=" + extraData +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(briefMessage, detailedMessage, quickfixes, extraData);
    }

    @NonNull
    public CharSequence getBriefMessage() {
        return briefMessage;
    }

    @Nullable
    public CharSequence getDetailedMessage() {
        return detailedMessage;
    }

    @Nullable
    public List<Quickfix> getQuickfixes() {
        return quickfixes;
    }

    @Nullable
    public Object getExtraData() {
        return extraData;
    }
}