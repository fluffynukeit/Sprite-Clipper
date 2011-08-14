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

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.imageio.ImageIO;


/**
 * Parent class for defining ways to pack sprites in a new sprite sheet.  Provides methods
 * that any kind of Packer can be expected to use.
 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public abstract class SpritePacker {
    private Map<SpriteClip, Point> clipToPosition = new HashMap<SpriteClip, Point>();
    private Rectangle desktop = new Rectangle();

    protected void setDesktopWidth(int w) {
        desktop.width = w;
    }
    protected void setDesktopHeight(int h) {
        desktop.height = h;
    }

    protected int getDesktopWidth() {
        return desktop.width;
    }
    protected int getDesktopHeight() {
        return desktop.height;
    }
    protected void clearPacker() {
        clipToPosition.clear();
    }

    protected final void addAt(SpriteClip clip, Point p){
        clipToPosition.put(clip, p);
        Rectangle newBox = new Rectangle(p.x,
                                        p.y,
                                        clip.getWidth(),
                                        clip.getHeight());
        desktop.add(newBox);
    }

    protected final int numAdded() {
        return clipToPosition.size();
    }

    private BufferedImage drawPack() {
        BufferedImage image = new BufferedImage(getDesktopWidth(),
                                                getDesktopHeight(),
                                                BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        Set<SpriteClip> keys = clipToPosition.keySet();
        for (SpriteClip currentClip : keys) {
            Point location = clipToPosition.get(currentClip);
            g.drawImage(currentClip.makeCutout(null, null),
                        location.x,
                        location.y,
                        null,
                        null);
        }
        return image;
    }

    public void writePack(File imageFile) throws IOException {
        
        BufferedImage image = drawPack();
        
        String inputName = imageFile.getPath();
        String imageFileName, baseName;
        String extension = ".png";
        if (inputName.endsWith(extension)) {
            baseName = inputName.substring(0, inputName.lastIndexOf(extension)).trim();
            imageFileName = baseName + extension;
        } else {
            imageFileName = inputName + extension;
            baseName = inputName;
        }
        File cleanFile = new File(imageFileName);
        ImageIO.write(image, "png", cleanFile);
        describePack(baseName, cleanFile);
    }

    protected void describePack(String baseName, File imageFile) throws IOException {
        FileWriter fstream = new FileWriter(baseName + ".def");
        PrintWriter out = new PrintWriter(fstream);
        
        out.println(imageFile.getName());
        
        Set<SpriteClip> clips = clipToPosition.keySet();
        
        for (SpriteClip c : clips ) {
            out.println("{");
            out.println("\t" + c.getName());
            Point p = clipToPosition.get(c);
            out.println("\t" + p.x);
            out.println("\t" + p.y);
            out.println("\t" + c.getWidth());
            out.println("\t" + c.getHeight());
            out.println("\t1");
            out.println("\t1");
            out.println("\t0");
            out.println("\t0");
            out.println("}");
        }
        out.flush();
        out.close();
    }

    public abstract void pack(List<SpriteClip> clips);
    public abstract String toString();
}
