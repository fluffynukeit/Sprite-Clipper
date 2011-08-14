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

package com.fluffynukeit.SpriteClipper.Processors;

import com.fluffynukeit.SpriteClipper.SpriteClip;
import com.fluffynukeit.SpriteClipper.SpritePacker;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;

/**
 * Packing method using a modification of the Heuristic Recursion packing algorithm.
 * 
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class HRPacker extends SpritePacker {

    private List<SpriteClip> clips;
    private int buffer = 1;

    private static Comparator<Rectangle> rectAreaComp = new Comparator<Rectangle>() {
            public int compare(Rectangle t, Rectangle other){
                return  -((t.width*t.height) - (other.width*other.height));
            }
        };

    public HRPacker() {
    }
    
    public HRPacker(int _buffer) {
        buffer = _buffer;
    }

    public void pack(List<SpriteClip> _clips){
        
        /* Only try to pack if we have stuff to pack. */
        if (_clips != null) {
            /*
             * We need to build an identical clip list to use, so that
             * if we can remove from the cloned list and not affect the
             * stored list.
             */
            List<SpriteClip> builtList = new ArrayList();
            for (SpriteClip curClip : _clips) {
                builtList.add(curClip);
            }
            clips = builtList;
            HeuristicRecursion();
        }
    }

    private void initialize() {
        
        clearPacker();
        Collections.sort(clips, SpriteClip.DEC_HEIGHT);
       
        //System.out.println("Initialize clips is " + out);
        
        /* Set the initial width of the image pack.  Assume all clip area fits in a square
         * and use square width as initial width (unless there is a clip with wider width)
         */
        int totalClipArea = 0;
        for (SpriteClip curClip : clips) {
            totalClipArea += (curClip.getWidth() * curClip.getHeight());
        }

        SpriteClip widestClip = Collections.min(clips, SpriteClip.DEC_WIDTH);
        int widestWidth = widestClip.getWidth();

        int initialWidth = (int) Math.max(Math.sqrt(totalClipArea), widestWidth);
        setDesktopWidth(initialWidth);
        setDesktopHeight(1);
    }

    private void RecursivePacking(Rectangle S2){

        //System.out.println("Recursive packing ==== Num added: " + numAdded());

        /* Find the first clip that can fit in s2 */
        SpriteClip addedClip = null;
        for (SpriteClip curClip : clips) {
            if (curClip.getWidth() <= (S2.width+1) &&
                curClip.getHeight() <= (S2.height+1)) {
                addedClip = curClip;
                break;
            }
        }
        if (addedClip == null) {
            return;
        }

        /* Otherwise we have a clip we can add */
        Point bottomRightCorner = packIn(addedClip, S2);
        
        /* Now split the remainder of S2 into sub areas */
        List<Rectangle> subAreas = splitArea(S2, bottomRightCorner);

        for (Rectangle curSubArea : subAreas) {
            RecursivePacking(curSubArea);
        }
    }

    private Point packIn(SpriteClip addedClip, Rectangle area) {

        //System.out.println("packIn area: " + area);

        Point setPoint = new Point(area.x, area.y);
        addAt(addedClip, setPoint);
        clips.remove(addedClip);

        int w = addedClip.getBoundingBox().width;
        int h = addedClip.getBoundingBox().height;

        // I think this stays the same...no adding of +1 here

        Point bottomRightCorner = new Point(setPoint.x + w,
                                            setPoint.y + h);

        //System.out.println("Adding " + addedClip.getName() + " at location " + setPoint +
        //                " with bottom corner " + bottomRightCorner);

        return bottomRightCorner;
    }

    private List<Rectangle> splitArea(Rectangle area, Point slicePoint) {

        /* First split horizontally */
        Rectangle bottom = new Rectangle(   area.x,
                                            slicePoint.y + buffer,
                                            area.width,
                                            area.y + area.height - (slicePoint.y + buffer));
        Rectangle top = new Rectangle(  slicePoint.x + buffer,
                                        area.y,
                                        area.x + area.width - (slicePoint.x + buffer),
                                        slicePoint.y - area.y);

        /* Now split vertically */
        Rectangle left = new Rectangle( area.x,
                                        slicePoint.y + buffer,
                                        slicePoint.x - area.x,
                                        area.y + area.height - (slicePoint.y + buffer));
        Rectangle right = new Rectangle(slicePoint.x + buffer,
                                        area.y,
                                        area.x + area.width - (slicePoint.x + buffer),
                                        area.height);
        /* Find the split that makes the avg aspect ratio closest to 1 */
        double horizontalAR = getAvgAR(bottom, top);
        double verticalAR = getAvgAR(left, right);

        List<Rectangle> returnMe = new ArrayList();

        if (Math.abs(verticalAR - 1) < Math.abs(horizontalAR - 1)) {
            // vert is closer to 1
            returnMe.add(left);
            returnMe.add(right);
        } else {
            returnMe.add(bottom);
            returnMe.add(top);
        }

        Collections.sort(returnMe, rectAreaComp);

        return returnMe;

    }

    private double getAR(Rectangle rect) {
        int a = Math.max(rect.width, rect.height);
        int b = Math.min(rect.width, rect.height);

        if (b == 0) {
            return Double.MAX_VALUE;
        } else {
            return (1.0*a)/b;
        }
    }

    private double getAvgAR(Rectangle a, Rectangle b) {
        return (getAR(a) + getAR(b))/2;
    }

    private void Packing(Rectangle S_in) {

        Rectangle S = S_in;
        Rectangle boundedSpace = new Rectangle();

        while(clips.size() > 0) {  //there are still clips to pack
            //System.out.println("Regular packing----Num added: " + numAdded());
            Point bottomCorner = packIn(clips.get(0),S);
            boundedSpace.setBounds( bottomCorner.x + buffer,
                                    S.y,
                                    S.width - (bottomCorner.x - S.x),
                                    bottomCorner.y - S.y);
             S.y = bottomCorner.y + buffer;  // shift the unbounded rectangle down
             RecursivePacking(boundedSpace);
        }
    }

    private void HeuristicRecursion() {
        initialize();
        //System.out.println("Desktop: " + getDesktopWidth() +"x"+getDesktopHeight());
        Rectangle S = new Rectangle(0,0, getDesktopWidth(), getDesktopHeight());
        Packing(S);

    }

    public String toString() {
        return "Heuristic recursion";
    }

}
