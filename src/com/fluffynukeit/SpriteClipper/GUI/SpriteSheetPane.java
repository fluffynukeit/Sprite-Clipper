/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fluffynukeit.SpriteClipper.GUI;

import com.fluffynukeit.SpriteClipper.*;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;

/**
 * Layered pane for interacting with identified sprites on sprite sheet.
 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class SpriteSheetPane extends JLayeredPane {

    public static final int IMAGE_LAYER = JLayeredPane.DEFAULT_LAYER;
    public static final int CLIP_LAYER = JLayeredPane.PALETTE_LAYER;

    public SpriteSheetPane(SpriteSheet spriteSheet) {

        int w = spriteSheet.getWidth();
        int h = spriteSheet.getHeight();

        setPreferredSize(new Dimension( w, h));

        ImageIcon icon = new ImageIcon(spriteSheet.getImage());
        JLabel label = new JLabel(icon);
        label.setBounds(0, 0, w, h);

        add(label);
        setLayer(label, IMAGE_LAYER);

    }

    public void markSpriteClips(Collection<SpriteClip> spriteClips) {
        /* Here we are going to redraw the sprite markers */
        
        clearMarkers();
        List<SpriteClip> sortedClips = new ArrayList(spriteClips);

        /* Sort the clips so smallest get placed on top of bigger ones */
        Collections.sort(sortedClips, Collections.reverseOrder(SpriteClip.DEC_AREA));

        /* Now draw the new markers */
        for (SpriteClip currentClip : sortedClips) {
            //System.out.println("Drawing clip");
            SpriteMarker newMarker = new SpriteMarker(currentClip);
            add(newMarker);
            setLayer(newMarker, CLIP_LAYER);
        }

    }

    public void clearMarkers() {
        /* First clear any markers already here */
        Component[] currentMarkers = getComponentsInLayer(CLIP_LAYER);

        for (Component marker : currentMarkers) {
            remove(getIndexOf(marker));
        }
    }

    public void setAllSelected(boolean bool) {
        

        /*
         * We want to select the clips in left-right, top-down order, but we don't want to
         * be too strict about it.
         */
        Component[] components = looselySortComponents();

        for (Component currentComponent : components) {
            SpriteMarker currentMarker = (SpriteMarker) currentComponent;
            if (currentMarker.getSelected() != bool) {
                currentMarker.setSelected(bool);
            }
        }
    }

    public List<SpriteClip> getSelectedClips() {
        Component[] components = getComponentsInLayer(CLIP_LAYER);

        List<SpriteMarker> selectedList = new ArrayList();

        for (Component currentComponent : components) {
            SpriteMarker currentMarker = (SpriteMarker) currentComponent;
            if (currentMarker.getSelected()) {
                selectedList.add(currentMarker);
            }
        }

        Collections.sort(selectedList); //sort selected markers by increasing ID

        List<SpriteClip> returnList = new ArrayList();
        for (SpriteMarker selectedMarker : selectedList ) {
            returnList.add(selectedMarker.getClip());
        }
        return returnList;
    }

    private Component[] looselySortComponents() {
        Component[] components = getComponentsInLayer(CLIP_LAYER);

        // Set up some sorters
        Comparator<Component> byY = new Comparator<Component>() {
            public int compare(Component a, Component b) {
                return  ((SpriteMarker)a).getClip().getBoundingBox().y -
                        ((SpriteMarker)b).getClip().getBoundingBox().y;
            }
        };

        Comparator<Component> byX = new Comparator<Component>() {
            public int compare(Component a, Component b) {
                return  ((SpriteMarker)a).getClip().getBoundingBox().x -
                        ((SpriteMarker)b).getClip().getBoundingBox().x;
            }
        };

        // First sort according to y coordinate, smallest to biggest
        Arrays.sort(components, byY);
        int rowStart = 0;

        while (rowStart < components.length) {
            // Choose a "model" sprite clip
            SpriteMarker model = (SpriteMarker)components[rowStart];
            int modelBottom  =  model.getClip().getY() +
                                model.getClip().getHeight();
            int indexNoOverlap = rowStart + 1;

            // Search for the first clip that does not overlap with the model
            for (   ;
                    indexNoOverlap < components.length &&
                    ((SpriteMarker)components[indexNoOverlap]).getClip().getBoundingBox().y < modelBottom;
                    indexNoOverlap++
                );
            // Sort the approved clips by x order
            Arrays.sort(components, rowStart, indexNoOverlap, byX);

            //Start the search again with the next lowest clip
            rowStart = indexNoOverlap;
        }
        return components;

    }

}
