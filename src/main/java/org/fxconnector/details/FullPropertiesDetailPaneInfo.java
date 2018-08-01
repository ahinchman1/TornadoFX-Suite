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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderImage;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.paint.Color;

import org.fxconnector.StageID;
import org.fxconnector.event.FXConnectorEventDispatcher;
import org.scenicview.utils.Logger;

@SuppressWarnings("rawtypes")
class FullPropertiesDetailPaneInfo extends DetailPaneInfo {

    boolean showCSSProperties = true;

    FullPropertiesDetailPaneInfo(final FXConnectorEventDispatcher dispatcher, final StageID stageID) {
        super(dispatcher, stageID, DetailPaneType.FULL);
    }

    Map<String, ObservableValue> orderedProperties;
    Map<String, Detail> fullPropertiesDetails;
    Map<WritableValue, String> styles;

    @Override protected String getPaneName() {
        return "Full Properties Details";
    }

    @Override Class<? extends Node> getTargetClass() {
        return null;
    }

    @Override public boolean targetMatches(final Object candidate) {
        return candidate != null;
    }

    @Override protected void createDetails() {
        // Nothing to do
    }

    @Override public void setTarget(final Object value) {
        if (doSetTarget(value)) {
            createPropertiesPanel();
        }

    }

    @SuppressWarnings("unchecked") private void createPropertiesPanel() {
        final Node node = (Node) getTarget();
        styles = new HashMap<>();
        details.clear();
        if (node != null) {
            final List<CssMetaData<? extends Styleable, ?>> list = node.getCssMetaData();
            for (final Iterator iterator = list.iterator(); iterator.hasNext();) {
                final CssMetaData cssMetaData = (CssMetaData) iterator.next();
                final WritableValue wvalue = cssMetaData.getStyleableProperty(node);
                styles.put(wvalue, cssMetaData.getProperty());
            }
        }

        orderedProperties = new TreeMap<>();
        fullPropertiesDetails = new HashMap<>();
        final Map<ObservableValue, String> properties = tracker.getProperties();
        for (final Iterator<ObservableValue> iterator = properties.keySet().iterator(); iterator.hasNext();) {
            final ObservableValue type = iterator.next();
            orderedProperties.put(properties.get(type), type);
        }
        for (final Iterator<String> iterator = orderedProperties.keySet().iterator(); iterator.hasNext();) {
            final String type = iterator.next();
            final String style = styles.get(orderedProperties.get(type));
            if (style == null || !showCSSProperties) {
                fullPropertiesDetails.put(type, addDetail(type, type + ":"));
            } else {
                fullPropertiesDetails.put(type, addDetail(type, type + "(" + style + "):"));
            }
        }
        updateAllDetails();
    }

    @Override protected void updateAllDetails() {
        if (orderedProperties != null) {
            for (final Iterator<String> iterator = orderedProperties.keySet().iterator(); iterator.hasNext();) {
                updateDetail(iterator.next(), true);
            }
        }
        sendAllDetails();
    }

    @Override protected void updateDetail(final String propertyName) {
        updateDetail(propertyName, false);
    }

    @SuppressWarnings({ "unchecked", "deprecation" }) protected void updateDetail(final String propertyName, final boolean all) {
        final Detail detail = fullPropertiesDetails.get(propertyName);
        final ObservableValue observable = orderedProperties.get(propertyName);
        final Object value = observable.getValue();
        if (value instanceof Image) {
            detail.setValue("Image (" + ((Image) value).impl_getUrl() + ")");
        } else if (value instanceof Background) {
            StringBuilder detailString = new StringBuilder("Background (");
            Background background = (Background)value;
            if (!background.getFills().isEmpty()) {
                detailString.append("fills=[");
                detailString.append(background.getFills().stream().map(FullPropertiesDetailPaneInfo::backgroundFillToString).collect(Collectors.joining(",\n  ")));
                detailString.append("]");
            }
            if (!background.getImages().isEmpty()) {
                detailString.append("images=[");
                detailString.append(background.getImages().stream().map(FullPropertiesDetailPaneInfo::backgroundImageToString).collect(Collectors.joining(",\n  ")));
                detailString.append("]");
            }

            detail.setValue(detailString.append(")").toString());
        } else if (value instanceof Border) {
            StringBuilder detailString = new StringBuilder("Border (");
            Border border = (Border) value;
            if (!border.getStrokes().isEmpty()) {
                detailString.append("strokes=[\n  ");
                detailString.append(border.getStrokes().stream().map(FullPropertiesDetailPaneInfo::borderStrokeToString).collect(Collectors.joining(",\n  ")));
                detailString.append("]");
            }
            if (!border.getImages().isEmpty()) {
                detailString.append("images=[\n  ");
                detailString.append(border.getImages().stream().map(FullPropertiesDetailPaneInfo::borderImageToString).collect(Collectors.joining(",\n  ")));
                detailString.append("]");
            }

            detail.setValue(detailString.append(")").toString());
        } else if (value instanceof Tooltip) {
            detail.setValue("Tooltip [text=\"" + ((Tooltip)value).getText() + "\"]");
        } else {
            detail.setValue(value == null ? Detail.EMPTY_DETAIL : value.toString());
            detail.setDefault(value == null);
        }

        if (observable instanceof Property) {
            if (observable.getValue() instanceof Enum) {
                detail.setEnumProperty((Property) observable, (Class<? extends Enum>) observable.getValue().getClass());
            } else if (!(observable instanceof ObjectProperty)) {
                detail.setSimpleProperty((Property) observable);
            } else if (observable.getValue() instanceof Color) {
                detail.setSimpleProperty((Property) observable);
            } else {
                detail.setSimpleProperty(null);
                detail.unavailableEdition(Detail.STATUS_NOT_SUPPORTED);
            }
        } else {
            detail.setSimpleProperty(null);
            if (observable instanceof ReadOnlyProperty) {
                detail.unavailableEdition(Detail.STATUS_READ_ONLY);
            } else {
                Logger.print("Strange Property:" + observable);
            }
        }
        if (!all)
            detail.updated();
    }
    
    private static String borderStrokeToString(BorderStroke borderStroke) {
        return "paint=" + topRightBottomLeft(borderStroke.getTopStroke(), borderStroke.getRightStroke(), borderStroke.getBottomStroke(), borderStroke.getLeftStroke())
            + "\n    style=" + topRightBottomLeft(borderStroke.getTopStyle(), borderStroke.getRightStyle(), borderStroke.getBottomStyle(), borderStroke.getLeftStyle())
            + "\n    widths=" + borderWidthsToString(borderStroke.getWidths())
            + " insets=" + borderStroke.getInsets()
            + " radii=" + borderStroke.getRadii();
    }
    
    private static String borderImageToString(BorderImage borderImage) {
        return "image=" + borderImage.getImage()
            + "\n    widths=" + borderWidthsToString(borderImage.getWidths())
            + "\n    slices=" + borderWidthsToString(borderImage.getSlices())
            + "\n    insets=" + borderImage.getInsets()
            + " repeatX=" + borderImage.getRepeatX()
            + " repeatY=" + borderImage.getRepeatY()
            + " filled=" + borderImage.isFilled();
    }

    private static String borderWidthsToString(BorderWidths widths) {
        if (widths == null) {
            return "null";
        }
        return topRightBottomLeft(
            individualBorderWidthToString(widths.getTop(), widths.isTopAsPercentage()),
            individualBorderWidthToString(widths.getRight(), widths.isRightAsPercentage()),
            individualBorderWidthToString(widths.getBottom(), widths.isBottomAsPercentage()),
            individualBorderWidthToString(widths.getLeft(), widths.isLeftAsPercentage())
        );
    }

    // If they are all the same according to .equals(), prints one value, else prints all four.
    private static <T> String topRightBottomLeft(T top, T right, T bottom, T left)
    {
        if (Objects.equals(top, right) && Objects.equals(top, bottom) && Objects.equals(top, left)) {
            return top.toString();
        } else {
            return top.toString() + " " + right.toString() + " " + bottom.toString() + " " + left.toString();
        }
    }

    private static String individualBorderWidthToString(double value, boolean percentage) {
        return value == BorderWidths.AUTO ? "AUTO" : value + (percentage ? "%" : "");
    }

    private static String backgroundFillToString(BackgroundFill backgroundFill) {
        return "paint=" + backgroundFill.getFill() 
            + " insets=" + backgroundFill.getInsets()
            + " radii=" + backgroundFill.getRadii();
    }

    private static String backgroundImageToString(BackgroundImage backgroundImage) {
        return "image=" + backgroundImage.getImage()
            + " position=" + backgroundPositionToString(backgroundImage.getPosition())
            + " repeatX=" + backgroundImage.getRepeatX()
            + " repeatY=" + backgroundImage.getRepeatY()
            + " size=" + backgroundSizeToString(backgroundImage.getSize());
    }

    private static String backgroundSizeToString(BackgroundSize size) {
        String width = size.getWidth() == BackgroundSize.AUTO ? "AUTO" : 
            size.getWidth() + (size.isWidthAsPercentage() ? "%" : "");
        String height = size.getHeight() == BackgroundSize.AUTO ? "AUTO" :
                size.getHeight() + (size.isHeightAsPercentage() ? "%" : "");
        return width + " x " + height
            + (size.isContain() ? " contain" : "")
            + (size.isCover() ? " cover" : "");
    }

    private static String backgroundPositionToString(BackgroundPosition position) {
        String horizPos = position.getHorizontalPosition() + (position.isHorizontalAsPercentage() ? "%" : "");
        String vertPos = position.getVerticalPosition() + (position.isVerticalAsPercentage() ? "%" : "");
        return position.getHorizontalSide() + " " + horizPos
                + " " + position.getVerticalSide() + " " + vertPos;
    }

    @Override void setShowCSSProperties(final boolean show) {
        showCSSProperties = show;
        if (getTarget() != null) {
            createPropertiesPanel();
        }
    }

}
