/*
 * Scenic View, 
 * Copyright (C) 2012 Jonathan Giles, Ander Ruiz, Amy Fowler 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fxconnector.details;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

import org.fxconnector.PropertyTracker;
import org.fxconnector.StageID;
import org.fxconnector.details.Detail.LabelType;
import org.fxconnector.details.Detail.ValueType;
import org.fxconnector.event.DetailsEvent;
import org.fxconnector.event.FXConnectorEvent.SVEventType;
import org.fxconnector.event.FXConnectorEventDispatcher;

abstract class DetailPaneInfo {

    private Object target;
    static DecimalFormat f = new DecimalFormat("0.0#");

    PropertyTracker tracker = new PropertyTracker() {
        @Override protected void updateDetail(final String propertyName, @SuppressWarnings("rawtypes") final ObservableValue property) {
            if (propertyName == null) return;
            DetailPaneInfo.this.updateDetail(propertyName);
        }

    };
    private final FXConnectorEventDispatcher dispatcher;
    private final DetailPaneType type;
    private int id;
    private final StageID stageID;
    protected final List<Detail> details = new ArrayList<>();

    DetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID, final DetailPaneType type) {
        this.dispatcher = dispatcher;
        this.stageID = stageID;
        this.type = type;
        createDetails();
    }

    abstract boolean targetMatches(Object target);

    void setTarget(final Object value) {
        if (doSetTarget(value)) {
            updateAllDetails();
        }
    }

    final void clear() {
        doSetTarget(null);
        final List<Detail> empty = Collections.emptyList();
        dispatcher.dispatchEvent(new DetailsEvent(SVEventType.DETAILS, stageID, type, getPaneName(), empty));
    }

    protected final boolean doSetTarget(final Object value) {
        if (target == value)
            return false;

        final Object old = target;
        if (old != null) {
            tracker.clear();
        }
        target = value;
        if (target != null) {
            tracker.setTarget(target);
        }
        return true;
    }

    final Object getTarget() {
        return target;
    }

    void setShowCSSProperties(final boolean show) {
    }

    protected String getPaneName() {
        return getTargetClass().getSimpleName() + " Details";
    }

    abstract Class<? extends Node> getTargetClass();

    protected final Detail addDetail(final String property, final String label) {
        return addDetail(property, label, ValueType.NORMAL);
    }

    protected final Detail addDetail(final String property, final String label, final ValueType type) {
        final Detail detail = new Detail(dispatcher, stageID, this.type, id++);
        detail.setProperty(property);
        detail.setLabel(label);
        detail.setValueType(type);
        detail.setDetailName(getPaneName());
        details.add(detail);
        return detail;
    }

    protected final Detail addDetail(final String property, final String label, final LabelType type) {
        final Detail detail = new Detail(dispatcher, stageID, this.type, id++);
        detail.setProperty(property);
        detail.setLabel(label);
        detail.setLabelType(type);
        detail.setDetailName(getPaneName());
        details.add(detail);
        return detail;
    }

    final void sendAllDetails() {
        dispatcher.dispatchEvent(new DetailsEvent(SVEventType.DETAILS, stageID, type, getPaneName(), details));
    }

    protected void updateAllDetails() {
        updateDetail("*");
    }

    protected abstract void updateDetail(final String propertyName);

    protected abstract void createDetails();

    final DetailPaneType getType() {
        return type;
    }

    final void setDetail(final int detailID, final String value) {
        for (int i = 0; i < details.size(); i++) {
            final Detail d = details.get(i);
            if (d.getDetailID() == detailID && d.serializer != null) {
                d.serializer.setValue(value);
                break;
            }
        }
    }
}
