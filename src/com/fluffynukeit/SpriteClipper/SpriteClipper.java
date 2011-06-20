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

import com.fluffynukeit.SpriteClipper.Processors.Connectivity8;
import com.fluffynukeit.SpriteClipper.Processors.CornerFilter;
import com.fluffynukeit.SpriteClipper.Processors.HRPacker;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;


/**
 * Class model of entire application.
 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class SpriteClipper extends Observable {
    private SpriteSheet currentSheet;
    private List<SpriteClip> storedClips;
    private SpriteClipFinder currentFinder;
    private BackgroundFilter currentFilter;
    private SpritePacker currentPacker;

    private final SpriteClipFinder[] availableFinders;
    private final BackgroundFilter[] availableFilters;
    private final SpritePacker[] availablePackers;

    /* Event identifiers */
    public static enum SpriteClipperEvent {
        SPRITESHEET_CHANGED, SPRITESHEET_CLEARED, 
        FINDER_CHANGED, FILTER_CHANGED,PACKER_CHANGED,
        CLIPS_FOUND, STORED_ADDED, STORED_REMOVED
    }


    public SpriteClipper() {
        SpriteClipFinder[] finders = {  new Connectivity8(),
                                        new Connectivity8(3),
                                        new Connectivity8(5)};
        BackgroundFilter[] filters = {new CornerFilter()};
        SpritePacker[] packers = {new HRPacker(3)};

        availableFinders = finders;
        availableFilters = filters;
        availablePackers = packers;

        storedClips = new ArrayList();
        currentFinder = finders[0];
        currentFilter = filters[0];
        currentPacker = packers[0];
    }

    public SpriteClipFinder[] getAvailableFinders() {
        return availableFinders;
    }

    public BackgroundFilter[] getAvailableFilters() {
        return availableFilters;
    }
    public SpritePacker[] getAvailablePackers() {
        return availablePackers;
    }

    public void setSpriteSheet(File file) throws Exception {
        try {
            currentSheet = new SpriteSheet(file);
        } catch (IOException e) {
            throw new Exception("Unable to open file " + file, e);
        }
        broadcastChange(SpriteClipperEvent.SPRITESHEET_CHANGED);
    }
    
    public SpriteSheet getSpriteSheet() {
        return currentSheet;
    }

    public void clearSpriteSheet() {
        currentSheet = null;
        broadcastChange(SpriteClipperEvent.SPRITESHEET_CLEARED);
    }

    public void setFinder(SpriteClipFinder newFinder) {
        currentFinder = newFinder;
        broadcastChange(SpriteClipperEvent.FINDER_CHANGED);
    }
    public SpriteClipFinder getFinder() {
        return currentFinder;
    }

    public BackgroundFilter getBackgroundFilter() {
        return currentFilter;
    }

    public void setBackgroundFilter(BackgroundFilter filter) {
        currentFilter = filter;
        broadcastChange(SpriteClipperEvent.FILTER_CHANGED);
    }

    public SpritePacker getSpritePacker() {
        return currentPacker;
    }

    public void setSpritePacker(SpritePacker packer) {
        currentPacker = packer;
        broadcastChange(SpriteClipperEvent.PACKER_CHANGED);
    }

    public void findSprites() {
        currentSheet.findClips(currentFinder, currentFilter);
        broadcastChange(SpriteClipperEvent.CLIPS_FOUND);
    }

    public void mergeSprites(Collection<SpriteClip> clips) {
        currentSheet.mergeClips(clips);
        broadcastChange(SpriteClipperEvent.CLIPS_FOUND);
    }
    
    public void reshapeSprites( Collection<SpriteClip> clips,
                                SpriteClip.AnchorType rsAnchorType) {
        currentSheet.expandBoxesOf(clips, rsAnchorType);
        broadcastChange(SpriteClipperEvent.CLIPS_FOUND);
    }

    public void storeClips(Collection<SpriteClip> _clips) {

        for (SpriteClip currentClip : _clips) {
            if (!storedClips.contains(currentClip)) {
                storedClips.add(currentClip);
            }
        }
        
        broadcastChange(SpriteClipperEvent.STORED_ADDED);
    }

    public void saveClips ( Collection<SpriteClip> clips, 
                            File directory,
                            String format) throws IOException {

        Collection<SpriteClip> useClips = (clips == null) ? storedClips:
                                                            clips;

        for (SpriteClip curClip : useClips) {
            curClip.saveTo(directory, format);
        }
    }

    public List<SpriteClip> getStoredClips() {

        return storedClips;
    }

    public void removeStoredClips(Collection<SpriteClip> clips) {
        if (clips != null) {
            storedClips.removeAll(clips);
            broadcastChange(SpriteClipperEvent.STORED_REMOVED);
        }
    }

    public void pack(Collection<SpriteClip> packClips, File destination) throws IOException{
        currentPacker.pack(packClips);
        currentPacker.writePack(destination);
    }

    private void broadcastChange(Object o) {
        setChanged();
        notifyObservers(o);
    }

    private void broadcastChange() {
        broadcastChange(null);
    }

}
