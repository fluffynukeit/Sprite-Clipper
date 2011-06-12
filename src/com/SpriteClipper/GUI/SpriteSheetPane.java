/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.SpriteClipper.GUI;

import com.SpriteClipper.*;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
        Component[] components = getComponentsInLayer(CLIP_LAYER);

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

}
