/*
 *  Copyright (c) 2011, Daniel Austin
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class GridPacker extends SpritePacker {
    private int buffer = 0;
    private int elementWidth;
    private int elementHeight;
    private int gridWidth;
    private int gridHeight;

    public GridPacker(int _buffer) {
        buffer = _buffer;
    }
    
    public GridPacker(){
        
    }

    public String toString() {
        return "Grid";
    }

    public void pack(List<SpriteClip> clips) {
        if (clips != null) {
            initialize(clips);
            doPack(clips);
        }
    }

    private void initialize(List<SpriteClip> clips) {
        clearPacker();
        SpriteClip widestClip = Collections.min(clips, SpriteClip.DEC_WIDTH);
        SpriteClip tallestClip = Collections.min(clips, SpriteClip.DEC_HEIGHT);

        int widestWidth = widestClip.getWidth();
        int tallestHeight = tallestClip.getHeight();

        int numClips = clips.size();
        gridWidth = (int) Math.ceil(Math.sqrt(numClips));
        gridHeight = (int) Math.ceil(numClips/(double)gridWidth);

        elementWidth = widestWidth;
        elementHeight = tallestHeight;
        
        int desktopWidth = gridWidth * (elementWidth + buffer);
        int desktopHeight = gridHeight * (elementHeight + buffer);

        setDesktopWidth(desktopWidth);
        setDesktopHeight(desktopHeight);
    }

    private void doPack(List<SpriteClip> clips) {
        
        int linearIndex = 0;
        for (SpriteClip curClip : clips) {
            Point insertionPoint = new Point();
            insertionPoint.y = (int) Math.floor((double)linearIndex/gridWidth) * (elementHeight + buffer);
            insertionPoint.x = (linearIndex % gridWidth) * (elementWidth + buffer);
            //System.out.println("Adding " + curClip.getName() + " at X " + insertionPoint.x
            //                                               + " and Y "+ insertionPoint.y);
            addAt(curClip, insertionPoint);
            linearIndex++;
        }
        
    }

    @Override
    protected void describePack(String baseName, File imageFile) throws IOException {
        super.describePack(baseName, imageFile);
        FileWriter fstream = new FileWriter(baseName + ".grid");
        PrintWriter out = new PrintWriter(fstream);

        out.println(imageFile.getName());
        out.println("Width of each element:                                " + elementWidth);
        out.println("Height of each element:                               " + elementHeight);
        out.println("Additonal buffer on bottom and right of each element: " + buffer);
        out.println("Total number of sprites:                              " + numAdded());
        out.println("Number of elements across:                            " + gridWidth);
        out.println("Number of elements down:                              " + gridHeight);

        out.flush();
        out.close();
    }
}
