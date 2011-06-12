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

package com.SpriteClipper.Processors;

import com.SpriteClipper.BackgroundFilter;
import com.SpriteClipper.Label;
import com.SpriteClipper.SimpleDisjoint;
import com.SpriteClipper.SpriteClip;
import com.SpriteClipper.SpriteClipFinder;
import java.awt.image.*;
import java.util.*;


/**
 * Blob detection algorithm using 8 connectivity (pixels can be diagonnally connected).
 * 
 * A half-edge length can be used to allow pixels that are within the half-edge distance
 * to be declared part of the same blob.
 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class Connectivity8 implements SpriteClipFinder {
    private SimpleDisjoint labelEquivalence = new SimpleDisjoint();
    private int nextID = 1;
    private int halfEdge = 1;
    
    public Connectivity8() {
        halfEdge = 1;
    }
    public Connectivity8(int _halfEdge) {
        setHalfEdge(_halfEdge);
    }

    public Label nextLabel() {
        return new Label(nextID++);
    }

    @Override
    public String toString(){
        if (halfEdge == 1) {
            return "8 connectivity";
        }
        else {
            return "8 conn. (" + halfEdge + "px HE)";
        }
        
    }

    public final void setHalfEdge(int _halfEdge) {
        if (_halfEdge < 1) {
            
            /*
             * We could do a throws Exception here instead of clipping to 1, but then
             * propgating the exception up the chain becomes a big pain...
             */
            _halfEdge = 1;
        }
        halfEdge = _halfEdge;
    }

    public Collection<SpriteClip> getSpriteClips(BufferedImage image, BackgroundFilter filter) {
        
        int h = image.getHeight();
        int w = image.getWidth();
        
        Label[][] labelSheet = new Label[h][w];
        
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int currentPixel = image.getRGB(col, row);
                if (filter.isForeground(currentPixel)) {
                    Label returnedLabel = labelFromNeighborhood(labelSheet, w, col, row);
                    labelSheet[row][col] = returnedLabel;

                }
            }
        }

        /* Now reduce the labels to their representatives and get pixels and bounding
         boxes. */

        Map<Label, SpriteClip> clipRegistry = new HashMap();

        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                Label currentLabel = labelSheet[row][col];
                if (currentLabel != null) { // then we have an object pixel at this loc.
                    Label rep =  labelEquivalence.getRep(currentLabel);
                    if (!clipRegistry.containsKey(rep)) {
                        SpriteClip sc = new SpriteClip(image, row, col, rep.toString());
                        clipRegistry.put(rep, sc);
                    } else {
                        SpriteClip sc = clipRegistry.get(rep);
                        sc.addLocation(row, col);
                    }
                }
            }
        }
        
        /* Now we have all the SpriteClips stored in a map.  Return them. */
        //return (Collection<SpriteClip>) clipRegistry.values();
        Set<SpriteClip> returnList = new HashSet();
        returnList.addAll(clipRegistry.values());
        return returnList;
        
        
    }
    
    private Label labelFromNeighborhood( Label[][] labelSheet,
                            int width,
                            int colCenter, 
                            int rowCenter) {
        
                
        int rowStart = Math.max(0, rowCenter - halfEdge);
        int colStart = Math.max(0, colCenter - halfEdge);
        int rowEnd = rowCenter;
        int colEnd = Math.min(width-1, colCenter + halfEdge);
        
        Label assignedLabel = null;
        
        rowLoop: for (int row = rowStart; row <= rowEnd; row++) {
            for (int col = colStart; col <= colEnd; col++) {
                
                /* If we've reached the center without labeling it, get new label. */
                if (    row == rowCenter && col == colCenter) {
                    if (assignedLabel == null){
                        assignedLabel = nextLabel();
                        labelEquivalence.addLabel(assignedLabel);
                    }
                    /* Skip checking of center and bottom right corner */
                    break rowLoop;
                }
                
                /** 
                 * Check neighbor's label (if any), and assign to the center if no 
                 * assignment has yet been made.
                 */
                Label neighbor = labelSheet[row][col];
                if (    neighbor != null && 
                        assignedLabel == null) {
                    
                    assignedLabel = neighbor;
                }
                
                /**
                 * Be sure to check all neighbors to mark equivalences.
                 */
                if (neighbor != null && neighbor != assignedLabel) {
                    if (!labelEquivalence.hasLabel(assignedLabel)) {
                        labelEquivalence.addLabel(assignedLabel);
                    }
                    labelEquivalence.setComembers(neighbor, assignedLabel);
                }
            }
        }
        
        return assignedLabel;
    }
        
}
