/*
 *  Copyright (c) 1998-2011, Daniel Austin
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of fluffynukeit.com nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 *  LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 *  A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER
 *  OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.SpriteClipper.GUI;

import com.SpriteClipper.SpriteClip;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.border.LineBorder;

/**
 * Graphic element for highlighting and selecting identified sprites in a SpriteSheet.
 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class SpriteMarker extends JLabel implements MouseListener,
                                                    Comparable {

    private SpriteClip clip;
    private boolean selected;
    private int selectID;
    private static int selectIDCounter = 0;

    public SpriteMarker(SpriteClip _clip) {
        clip = _clip;
        drawMarker(Color.MAGENTA);
        addMouseListener(this);
    }
    
    private void drawMarker(Color color) {
        
        int alpha = 128;
        Color clipColor = new Color(color.getRed(),
                                    color.getGreen(),
                                    color.getBlue(),
                                    alpha);
        
        BufferedImage clipImage = clip.makeCutout(clipColor, null);

        ImageIcon icon = new ImageIcon(clipImage);
        setIcon(icon);

        Rectangle box = clip.getBoundingBox();
        setBounds(box.x, box.y, box.width+1, box.height+1);
        setBorder(new LineBorder(color));
        setVisible(true);
    }

    public boolean setSelected(boolean _selected) {

        selected = _selected;

        if (!selected) {
            drawMarker(Color.MAGENTA);
        } else {
            selectID = selectIDCounter++;
            drawMarker(Color.GREEN);
        }

        return selected;
    }

    public boolean getSelected() {
        return selected;
    }

    public int compareTo(Object other) {
        return selectID - ((SpriteMarker) other).selectID;
    }

    public SpriteClip getClip() {
        return clip;
    }

    public void mouseClicked(MouseEvent event) {
    }
    public void mouseEntered(MouseEvent event) {
        drawMarker(Color.GREEN);
    }
    public void mouseExited(MouseEvent event) {
        if (!selected) {
            drawMarker(Color.MAGENTA);
        } else {
            drawMarker(Color.GREEN);
        }
    }
    public void mousePressed(MouseEvent event) {
    }
    public void mouseReleased(MouseEvent event) {
        selected = setSelected(!selected);
    }


}
