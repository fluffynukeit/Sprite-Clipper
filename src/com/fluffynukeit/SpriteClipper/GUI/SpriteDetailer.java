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

/*
 * SpriteDetailer.java
 *
 * Created on May 18, 2011, 8:20:08 PM
 */

package com.fluffynukeit.SpriteClipper.GUI;

import com.fluffynukeit.SpriteClipper.SpriteClip;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.PlainDocument;

/**
 * Small widget for exploring, examining, and naming clipped sprites.
 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class SpriteDetailer extends javax.swing.JPanel implements ListSelectionListener {

    /** Creates new form SpriteDetailer */
    public SpriteDetailer() {
        initComponents();
        //System.out.println("init");
        labelText.setText("No stored clips available.");
        RenameFilter fnf = new RenameFilter();
        ((PlainDocument)textField.getDocument()).setDocumentFilter(fnf);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        labelImage = new javax.swing.JLabel();
        textField = new javax.swing.JTextField();
        labelText = new javax.swing.JLabel();

        labelImage.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelImage.setVerticalAlignment(javax.swing.SwingConstants.BOTTOM);
        labelImage.setFocusable(false);
        labelImage.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        labelImage.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        textField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        textField.setToolTipText("<html><div align=\"center\">Select a clipped sprite and enter a name here.<br>Be sure the name is unique!</div></html>");
        textField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textFieldActionPerformed(evt);
            }
        });

        labelText.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(labelText, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .addComponent(labelImage, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                    .addComponent(textField, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelImage, javax.swing.GroupLayout.DEFAULT_SIZE, 76, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelText, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void textFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textFieldActionPerformed
        clip.setName(textField.getText());
        updateLabelText();
}//GEN-LAST:event_textFieldActionPerformed
    public void valueChanged(ListSelectionEvent e) {
        JList list = (JList)e.getSource();
        int listSize = list.getModel().getSize();

        int selectedIndices[] = list.getSelectedIndices();
        int numIndices = selectedIndices.length;

        if (numIndices == 0 || numIndices > 1) {
            setClip(null);
            String selText =    (numIndices != 0) ? numIndices + " clips selected.":
                                (listSize != 0) ?   "All " + listSize + " clips will be used.":
                                                    "No stored clips available.";
            labelText.setText(selText);
        } else {
            setClip(((SpriteClip) list.getSelectedValue()));
        }

    }

    private void updateLabelText(){
        Icon icon = labelImage.getIcon();
        int lw = icon.getIconWidth();
        int lh = icon.getIconHeight();
        Rectangle box = clip.getBoundingBox();
        int percentSize = (lw*lh*100)/((box.width)*(box.height));
        labelText.setText(  "<html><div align=\"center\">" +
                            clip.getName() + "<br>" + percentSize + "% scale, " +
                            "full size " + box.width + "x" + box.height +
                            "</div></html>");
        //reset focus?
        textField.setFocusable(false);
        textField.setFocusable(true);
    }

    private void setClip(SpriteClip _clip) {
        clip = _clip;
       
        if (clip != null) {
            textField.setEnabled(true);
            Dimension dim = new Dimension(labelImage.getWidth(), labelImage.getHeight());
            Image scaledImage = clip.makeCutout(null, dim);

            Icon icon = new ImageIcon(scaledImage);
            labelImage.setIcon(icon);
            updateLabelText();

            /* Now set the text field */
            textField.setText(clip.getName());
        } else {
            /* Clear out the sprite detailer */
            labelImage.setIcon(null);
            labelText.setText(null);
            textField.setText(null);
            textField.setEnabled(false);
        }
    }

    private SpriteClip clip = null;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel labelImage;
    private javax.swing.JLabel labelText;
    private javax.swing.JTextField textField;
    // End of variables declaration//GEN-END:variables

}
