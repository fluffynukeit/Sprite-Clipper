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

package com.fluffynukeit.SpriteClipper;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * Defines the set of points that make up an identified sprite, and labeling info.
 * 
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public final class SpriteClip {
    private Set<Point> points = new HashSet();
    private String name;
    private Rectangle boundingBox;
    private BufferedImage parentImage;

    public static enum AnchorType {
        TL, TC, TR, CL, CR, BL, BC, BR; /* reshape anchors */
    }

    public SpriteClip(BufferedImage _parentImage, int row, int col, String _name) {
        parentImage = _parentImage;
        name = _name;
        addLocation(row, col);
    }

    public SpriteClip(BufferedImage _parentImage, String _name) {
        parentImage = _parentImage;
        name = _name;
    }

    public BufferedImage makeCutout(Color maskColor, Dimension dim) {
        /* first get all the data from the image */
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();

        Rectangle box = getBoundingBox();
        int w = box.width;
        int h = box.height;

        BufferedImage clipImage =
                gc.createCompatibleImage(w+1, h+1, Transparency.TRANSLUCENT);
        BufferedImage parent = getParentImage();

        for (Point curPoint: getPoints()) {
            int relX = getRelX(curPoint);
            int relY = getRelY(curPoint);
            
            int curPixel = (maskColor != null) ?    maskColor.getRGB():
                                                    parent.getRGB(curPoint.x, curPoint.y);
            clipImage.setRGB(relX, relY, curPixel);
        }
        if (dim == null){
            return clipImage;
        } else { // we need to do some scaling
            int lw = dim.width;
            int lh = dim.height;
            int scaledWidth = w;
            int scaledHeight = h;
            if (w >= lw && h <= lh){
                //then set the scaled width to be the label width
                scaledHeight = (int) (1.0*lw/w*h);
                scaledWidth = lw;
            }else if (h >= lh && w <= lw){
                // scale the height to fit inside the label
                scaledWidth = (int) (1.0*lh/h*w);
                scaledHeight = lh;
            }else if (h >= lh && w >= lw){
                /* then we need to pick the right thing to scale.  Pick the dimension that is
                 * the largest relative to the label dimension.
                 */

                if (h*1.0/lh > w*1.0/lw){
                    scaledWidth = (int) (1.0*lh/h*w);
                    scaledHeight = lh;
                }else {
                    scaledHeight = (int) (1.0*lw/w*h);
                    scaledWidth = lw;
                }
            }

            /* Now do the image drawing */
            BufferedImage clipImageScaled =
                gc.createCompatibleImage(scaledWidth, scaledHeight, Transparency.TRANSLUCENT);
            clipImageScaled.createGraphics().setRenderingHint(
                                                RenderingHints.KEY_RENDERING,
                                                RenderingHints.VALUE_RENDER_QUALITY);
            clipImageScaled.getGraphics().drawImage(    clipImage,
                                                        0,
                                                        0,
                                                        scaledWidth,
                                                        scaledHeight,
                                                        null);
            return clipImageScaled;
        }
        
    }

    public void saveTo(File directory, String format) throws IOException{
        File outfile = new File(directory, name + "." + format);
        ImageIO.write(makeCutout(null, null), format, outfile);
    }

    public static SpriteClip makeMergedClip(Collection<SpriteClip> clips) {
        /* Get the image and name from one of the clips.  Assumes all are from the same
         parent image.*/
        Iterator<SpriteClip> iter = clips.iterator();
        if (iter.hasNext()) {
            SpriteClip template = iter.next();

            SpriteClip mergedClip = new SpriteClip( template.getParentImage(),
                                                    template.getName());
            for (SpriteClip currentClip : clips) {
                mergedClip.copyPointData(currentClip);
            }
            return mergedClip;
        } else {
            return null;
        }
    }

    private void copyPointData(SpriteClip clip) {
        Collection<Point> pointsCollection = clip.getPoints();
        for (Point currentPoint : pointsCollection) {
            addLocation(currentPoint.y, currentPoint.x);
        }
    }

    public void addLocation(int row, int col){
        Point p = new Point(col, row);
        points.add(p);
        if (boundingBox == null) {
            boundingBox = new Rectangle(col, row, 1, 1);
        } else {
            boundingBox.add(p);
        }
    }

    public void reshapeBox(int newHeight, int newWidth, AnchorType rsAnchorType){
        
        int pw = parentImage.getWidth();
        int ph = parentImage.getHeight();
        int anchorX = 0;
        int anchorY = 0;
        int shiftLeft = 0;
        int shiftRight = 0;
        int shiftUp = 0;
        int shiftDown = 0;
        switch (rsAnchorType) {
            case BL:
            case BC:
            case BR:
                anchorY = boundingBox.y + boundingBox.height;
                shiftDown = 0;
                shiftUp = newHeight;
                break;
            case CL:
            case CR:
                anchorY = boundingBox.y + (int) (boundingBox.height/2.0);
                shiftDown = (int) (newHeight/2.0);
                shiftUp = newHeight - shiftDown;
                break;
            case TL:
            case TC:
            case TR:
                anchorY = boundingBox.y;
                shiftDown = newHeight;
                shiftUp = 0;
                break;
        }
        switch (rsAnchorType) {
            case TL:
            case BL:
            case CL:
                anchorX = boundingBox.x;
                shiftLeft = 0;
                shiftRight = newWidth;
                break;
            case TC:
            case BC:
                anchorX = boundingBox.x + (int) (boundingBox.width/2.0);
                shiftLeft = (int) (newWidth/2.0);
                shiftRight = newWidth - shiftLeft;
                break;
            case TR:
            case CR:
            case BR:
                anchorX = boundingBox.x + boundingBox.width;
                shiftLeft = newWidth;
                shiftRight = 0;
                break;
        }


        Rectangle bounder = new Rectangle(anchorX, anchorY, 1, 1);

        int newUp = Math.max(anchorY - shiftUp , 0);
        int newRight = Math.min(anchorX + shiftRight, pw-1);
        int newDown = Math.min(anchorY + shiftDown, ph-1);
        int newLeft = Math.max(anchorX - shiftLeft, 0);

        bounder.add(newRight, newUp);
        bounder.add(newLeft, newDown);

        boundingBox.setBounds(bounder);

    }

    public int getRelX(Point p){
        int relX = p.x-boundingBox.x;
        return relX;
    }

    public int getRelY(Point p){
        int relY = p.y-boundingBox.y;
        return relY;
    }
    public Rectangle getBoundingBox() {
        return boundingBox;
    }
    public Set<Point> getPoints() {
        return points;
    }

    public BufferedImage getParentImage() {
        return parentImage;
    }

    public String getName() {
        return name;
    }
    public void setName(String newName) {
        name = newName;
    }

    /* Sorts by decreasing area */
    public static Comparator<SpriteClip> DEC_AREA = new Comparator<SpriteClip>() {
            public int compare(SpriteClip t, SpriteClip other){

            return  -((t.getBoundingBox().width*t.getBoundingBox().height) -
                    (other.getBoundingBox().width*other.getBoundingBox().height));
            }
        };
    /* Sorts by decreasing height */
    public static Comparator<SpriteClip> DEC_HEIGHT = new Comparator<SpriteClip>() {
        public int compare(SpriteClip t, SpriteClip other){

        return  -(t.getBoundingBox().height - other.getBoundingBox().height);
        }
    };
    /* Sorts by decreasing width */
    public static Comparator<SpriteClip> DEC_WIDTH = new Comparator<SpriteClip>() {
            public int compare(SpriteClip a, SpriteClip b) {
                return -(a.getBoundingBox().width - b.getBoundingBox().width);
            }
        };
}
