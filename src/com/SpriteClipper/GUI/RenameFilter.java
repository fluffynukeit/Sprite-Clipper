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

import javax.swing.text.*;

/**
 * Filter for preventing certain character sequences when naming sprites.
 *
 * Copied from http://stackoverflow.com/questions/5793606/how-to-filter-illegal-forbidden-filename-characters-from-users-keyboard-input-in/5808420#5808420
 * and then modified a little based on NetBeans recommendations.

 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class RenameFilter extends DocumentFilter
{
    private static final char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '.'};

    @Override
    public void insertString (DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException
    {
        fb.insertString (offset, fixText(text, fb), (javax.swing.text.AttributeSet) attr);
    }

    @Override
    public void replace (DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException
    {
        fb.replace(offset, length, fixText(text, fb), (javax.swing.text.AttributeSet) attr);
    }

    private String fixText (String s, DocumentFilter.FilterBypass  fb) throws BadLocationException
    {
        if (s == null) {
            return null;
        }

        int currentLength = fb.getDocument().getLength();
        int l = fb.getDocument().getLength();
        char prevChar = (l < 1) ? ' ' : fb.getDocument().getText(l-1, 1).charAt(0);

        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < s.length(); ++i)
        {
            boolean doubleSpace = prevChar == ' ' && s.charAt(i) == ' ';
            
            if (!isIllegalFileNameChar (s.charAt (i)) &&
                !doubleSpace &&
                currentLength < 25) //hard coded 25 char max
                sb.append (s.charAt (i));
        }
        return sb.toString();
    }

    private boolean isIllegalFileNameChar (char c)
    {
        boolean isIllegal = false;
        for (int i = 0; i < ILLEGAL_CHARACTERS.length; i++)
        {
            if (c == ILLEGAL_CHARACTERS[i])
                isIllegal = true;
        }
        return isIllegal;
    }
}

