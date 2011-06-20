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

import java.awt.Rectangle;
import java.awt.image.*;
import java.io.*;
import java.util.Collection;
import java.util.HashSet;
import javax.imageio.*;

/**
 * Container object for finding and storing SpriteClips.
 * 
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class SpriteSheet {
    private BufferedImage sheet;
    private File file;
    private Collection<SpriteClip> clips;

    public SpriteSheet(File src) throws IOException {
        file = src;
        sheet = ImageIO.read(file);
    }

    public File getFile() {
        return file;
    }

    public int getWidth(){
        return sheet.getWidth();
    }
    public int getHeight(){
        return sheet.getHeight();
    }

    public void findClips(SpriteClipFinder clipFinder, BackgroundFilter filter) {
        filter.declareImage(sheet);
        clips = clipFinder.getSpriteClips(sheet, filter);
    }

    public void mergeClips(Collection<SpriteClip> mergeClips) {

        if (mergeClips == null) {
            return;
        }

        if (mergeClips.size() == 1) {
            /**
             * If only 1 clip is selected, then find overlapping clips and merge them.
             */
            SpriteClip selectedClip = mergeClips.iterator().next();

            Collection<SpriteClip> overlappingSprites = new HashSet();
            overlappingSprites.add(selectedClip);
            for (SpriteClip currentClip : clips) {
                if (selectedClip != currentClip &&
                    currentClip.getBoundingBox().intersects(selectedClip.getBoundingBox())) {
                    overlappingSprites.add(currentClip);
                }
            }
            /* Once done, only merge if 2 sprites overlap */
            if (overlappingSprites.size() >= 2) {
                clips.removeAll(overlappingSprites);
                SpriteClip newMergedClip = SpriteClip.makeMergedClip(overlappingSprites);
                clips.add(newMergedClip);
            }
        } else {    // merge the selected clips
            clips.removeAll(mergeClips);
            SpriteClip newMergedClip = SpriteClip.makeMergedClip(mergeClips);
            clips.add(newMergedClip);
        }
    }

    public void expandBoxesOf(  Collection<SpriteClip> expandClips,
                                SpriteClip.AnchorType rsAnchorType) {

        if (expandClips == null || expandClips.size() <= 1) {
            return;     // no expanding needed to be done
        }

        /* First iterate through and find the clip that has the biggest height and width */
        SpriteClip maxHeightClip = null;
        SpriteClip maxWidthClip = null;
        for (SpriteClip currentClip : expandClips) {
            Rectangle currentBox = currentClip.getBoundingBox();
            if (maxHeightClip == null ||
                currentBox.height > maxHeightClip.getBoundingBox().height){
                    maxHeightClip = currentClip;
            }
            if (maxWidthClip == null ||
                currentBox.width > maxWidthClip.getBoundingBox().width){
                    maxWidthClip = currentClip;
            }
        }

        int newWidth = maxWidthClip.getBoundingBox().width;
        int newHeight = maxHeightClip.getBoundingBox().height;

        /* Now expand the heights and widths so that they're all equal */
        for (SpriteClip currentClip : expandClips){
            if (currentClip != maxHeightClip ||
                currentClip != maxWidthClip) { // then expand size
                currentClip.reshapeBox(newHeight, newWidth, rsAnchorType);
            }

        }
    }

    public Collection<SpriteClip> getClips() {
        return clips;
    }

    public BufferedImage getImage(){
        return sheet;
    }

}
