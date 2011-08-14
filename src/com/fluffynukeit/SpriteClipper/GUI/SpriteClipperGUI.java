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
 * SpriteClipperGUI.java
 *
 * Created on May 8, 2011, 9:30:44 PM
 */

package com.fluffynukeit.SpriteClipper.GUI;


import com.fluffynukeit.SpriteClipper.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Main GUI.  Based on SpriteClipper application model.
 *
 * @author Daniel Austin <dan@fluffynukeit.com>
 */
public class SpriteClipperGUI extends javax.swing.JFrame implements Observer,
                                                                    KeyListener {

    /** Creates new form SpriteClipperGUI */
    public SpriteClipperGUI() {
        initComponents();

        /* Set up the button group */
        rsAnchorGroup = new ButtonGroup();
        rsAnchorGroup.add(tlRadioButton);
        rsAnchorGroup.add(tcRadioButton);
        rsAnchorGroup.add(trRadioButton);
        rsAnchorGroup.add(clRadioButton);
        rsAnchorGroup.add(crRadioButton);
        rsAnchorGroup.add(blRadioButton);
        rsAnchorGroup.add(bcRadioButton);
        rsAnchorGroup.add(brRadioButton);
        rsAnchorGroup.setSelected(blRadioButton.getModel(), true);

        connSelector.setSelectedIndex(1);

        spriteClipper.addObserver(this);
        clippedList.addListSelectionListener(spriteDetailer);
        addKeyListener(this);
    }

    public void update(Observable obs, Object obj) {
        try {
            SpriteClipper.SpriteClipperEvent event = (SpriteClipper.SpriteClipperEvent) obj;

            switch (event) {
                case SPRITESHEET_CHANGED:
                    SpriteSheet newSheet = spriteClipper.getSpriteSheet();
                    SpriteSheetPane newSSPane = new SpriteSheetPane(newSheet);
                    spriteSheetScrollPane.setViewportView(newSSPane);
                    closeButton.setEnabled(true);
                    findSpritesButton.setEnabled(true);
                    sheetPanel.setBorder(new TitledBorder("Sprite Sheet (" +
                                                                    newSheet.getFile() +
                                                                    ")"));
                    break;
                case SPRITESHEET_CLEARED:
                    spriteSheetScrollPane.setViewportView(null);
                    closeButton.setEnabled(false);
                    findSpritesButton.setEnabled(false);
                    sheetPanel.setBorder(new TitledBorder("Sprite Sheet"));
                    setSpriteControlsEnabled(false);
                    break;
                case FINDER_CHANGED:
                    connSelector.setSelectedItem(spriteClipper.getFinder());
                    break;
                case FILTER_CHANGED:
                    filterSelector.setSelectedItem(spriteClipper.getBackgroundFilter());
                    break;
                case PACKER_CHANGED:
                    packerSelector.setSelectedItem(spriteClipper.getSpritePacker());
                    break;
                case CLIPS_FOUND:
                    SpriteSheetPane ssPane = (SpriteSheetPane)
                                             spriteSheetScrollPane.getViewport().getView();
                    ssPane.markSpriteClips(spriteClipper.getSpriteSheet().getClips());
                    setSpriteControlsEnabled(true);
                    break;
                case STORED_ADDED:
                {
                    DefaultListModel listModel = (DefaultListModel) clippedList.getModel();
                    int[] selInds = clippedList.getSelectedIndices();
                    List<SpriteClip> storedClips = spriteClipper.getStoredClips();

                    /*
                     * Clearing, re-adding all, then re-selecting is faster than checking
                     * if each stored clip is already in the list model.  If the event
                     * contained only the new clips, we could just add the new ones, but
                     * doesn't that break MVC paradigm? Ideology fail.
                     */
                    listModel.clear();
                    for (SpriteClip currentClip : storedClips) { 
                        listModel.addElement(currentClip);
                    }
                    clippedList.setSelectedIndices(selInds);
                    /* STORED_ADDED only triggered if new clips are actually added. */
                    clippedList.ensureIndexIsVisible(listModel.getSize()-1);
                    setSaveControlsEnabled(true);

                    /* Deselect everything as feedback for add operation */
                    SpriteSheetPane ssPaneAdded = (SpriteSheetPane)
                                             spriteSheetScrollPane.getViewport().getView();
                    if (ssPaneAdded != null) {
                        ssPaneAdded.setAllSelected(false);
                    }
                    spriteDetailer.valueChanged(new ListSelectionEvent(clippedList,
                                                                    0,
                                                                    listModel.getSize()-1,
                                                                    false));
                    break;
                }
                case STORED_REMOVED:
                {
                    DefaultListModel listModel = (DefaultListModel) clippedList.getModel();

                    List<SpriteClip> storedClips = spriteClipper.getStoredClips();

                    /*
                     * Clearing and re-adding is faster than querying to see which clips
                     * should be removed from the list model.
                     */
                    listModel.clear();
                    for (SpriteClip currentClip : storedClips) {
                        listModel.addElement(currentClip);
                    }

                    /* Only allow saving if clips are available. */
                    if (listModel.isEmpty()) {
                        setSaveControlsEnabled(false);
                    }
                    spriteDetailer.valueChanged(new ListSelectionEvent(clippedList,
                                                                    0,
                                                                    listModel.getSize()-1,
                                                                    false));
                    break;
                }

                default:
            }
        } catch (Exception e) {
            exceptionDialog(e);
        }

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rsAnchorGroup = new javax.swing.ButtonGroup();
        sheetPanel = new javax.swing.JPanel();
        spriteSheetScrollPane = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        selectNoneButton = new javax.swing.JButton();
        selectAllButton = new javax.swing.JButton();
        findSpritesButton = new javax.swing.JButton();
        openButton = new javax.swing.JButton();
        reshapeButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        mergeButton = new javax.swing.JButton();
        connLabel = new javax.swing.JLabel();
        connSelector = new javax.swing.JComboBox();
        storeClipsButton = new javax.swing.JButton();
        filterLabel = new javax.swing.JLabel();
        filterSelector = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        trRadioButton = new javax.swing.JRadioButton();
        tlRadioButton = new javax.swing.JRadioButton();
        clRadioButton = new javax.swing.JRadioButton();
        crRadioButton = new javax.swing.JRadioButton();
        tcRadioButton = new javax.swing.JRadioButton();
        brRadioButton = new javax.swing.JRadioButton();
        blRadioButton = new javax.swing.JRadioButton();
        bcRadioButton = new javax.swing.JRadioButton();
        storedPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        clippedList = new javax.swing.JList();
        removeButton = new javax.swing.JButton();
        saveToButton = new javax.swing.JButton();
        spriteDetailer = new com.fluffynukeit.SpriteClipper.GUI.SpriteDetailer();
        packButton = new javax.swing.JButton();
        packerLabel = new javax.swing.JLabel();
        packerSelector = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Sprite Clipper");
        setBounds(new java.awt.Rectangle(0, 0, 800, 600));
        setFocusable(false);
        setName("mainFrame"); // NOI18N

        sheetPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Sprite Sheet"));
        sheetPanel.setFocusable(false);

        spriteSheetScrollPane.setFocusable(false);

        jPanel1.setFocusable(false);

        selectNoneButton.setText("None");
        selectNoneButton.setToolTipText("<html><div align=\"center\">Deselects all found sprites. (Ctrl+N)</div></html>");
        selectNoneButton.setEnabled(false);
        selectNoneButton.setFocusable(false);
        selectNoneButton.setName("findSpritesButton"); // NOI18N
        selectNoneButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectNoneButtonActionPerformed(evt);
            }
        });

        selectAllButton.setText("All");
        selectAllButton.setToolTipText("<html><div align=\"center\">Selects all found sprites. (Ctrl+A)</div></html>");
        selectAllButton.setEnabled(false);
        selectAllButton.setFocusable(false);
        selectAllButton.setName("findSpritesButton"); // NOI18N
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        findSpritesButton.setFont(new java.awt.Font("DejaVu Sans", 0, 14));
        findSpritesButton.setText("<html><div align=\"center\">Find<br>Sprites!</div></html>");
        findSpritesButton.setToolTipText("<html><div align=\"center\">Identifies sprites in the sprite sheet using selected <br> background filter and connectedness criterion. (Ctrl+F)</div></html>");
        findSpritesButton.setEnabled(false);
        findSpritesButton.setFocusable(false);
        findSpritesButton.setName("findSpritesButton"); // NOI18N
        findSpritesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findSpritesButtonActionPerformed(evt);
            }
        });

        openButton.setText("Open");
        openButton.setToolTipText("<html><div align=\"center\">Opens a sprite sheet. (Ctrl+O)</div></html>");
        openButton.setFocusable(false);
        openButton.setName("openButton"); // NOI18N
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        reshapeButton.setText("Reshape");
        reshapeButton.setToolTipText("<html><div align=\"center\">Select multiple sprites on the sheet, an anchor point, then press reshape.<br>The bounds of all selected clips will be expanded to the same size. (Ctrl+R)</div></html>");
        reshapeButton.setEnabled(false);
        reshapeButton.setFocusable(false);
        reshapeButton.setName("findSpritesButton"); // NOI18N
        reshapeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reshapeButtonActionPerformed(evt);
            }
        });

        closeButton.setText("Close");
        closeButton.setToolTipText("<html><div align=\"center\">Closes the open sprite sheet. (Ctrl+W)</div></html>");
        closeButton.setEnabled(false);
        closeButton.setFocusable(false);
        closeButton.setName("closeButton"); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        mergeButton.setText("Merge");
        mergeButton.setToolTipText("<html><div align=\"center\">Combines multiple selected sprites. (Ctrl+M)</div></html>");
        mergeButton.setEnabled(false);
        mergeButton.setFocusable(false);
        mergeButton.setName("findSpritesButton"); // NOI18N
        mergeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mergeButtonActionPerformed(evt);
            }
        });

        connLabel.setText("Connected criterion:");

        connSelector.setModel(new DefaultComboBoxModel(spriteClipper.getAvailableFinders()));
        connSelector.setFocusable(false);
        connSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connSelectorActionPerformed(evt);
            }
        });

        storeClipsButton.setFont(new java.awt.Font("DejaVu Sans", 0, 14));
        storeClipsButton.setText("Clip!");
        storeClipsButton.setToolTipText("<html><div align=\"center\">Adds selected sprites to the clipped sprites. (Ctrl+C)</div></html>");
        storeClipsButton.setEnabled(false);
        storeClipsButton.setFocusable(false);
        storeClipsButton.setName("findSpritesButton"); // NOI18N
        storeClipsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeClipsButtonActionPerformed(evt);
            }
        });

        filterLabel.setText("Background is...");

        filterSelector.setModel(new DefaultComboBoxModel(spriteClipper.getAvailableFilters()));
        filterSelector.setFocusable(false);
        filterSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterSelectorActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("RS Anchor"));
        jPanel2.setFocusable(false);

        trRadioButton.setEnabled(false);
        trRadioButton.setFocusable(false);

        tlRadioButton.setEnabled(false);
        tlRadioButton.setFocusable(false);

        clRadioButton.setEnabled(false);
        clRadioButton.setFocusable(false);

        crRadioButton.setEnabled(false);
        crRadioButton.setFocusable(false);

        tcRadioButton.setEnabled(false);
        tcRadioButton.setFocusable(false);

        brRadioButton.setEnabled(false);
        brRadioButton.setFocusable(false);

        blRadioButton.setEnabled(false);
        blRadioButton.setFocusable(false);

        bcRadioButton.setEnabled(false);
        bcRadioButton.setFocusable(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addComponent(clRadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(crRadioButton))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addComponent(tlRadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(tcRadioButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(trRadioButton)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(blRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bcRadioButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(brRadioButton)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tlRadioButton)
                    .addComponent(tcRadioButton)
                    .addComponent(trRadioButton))
                .addGap(6, 6, 6)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(clRadioButton)
                    .addComponent(crRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bcRadioButton)
                    .addComponent(blRadioButton)
                    .addComponent(brRadioButton)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(openButton)
                        .addGap(8, 8, 8)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(filterLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)
                            .addComponent(filterSelector, 0, 145, Short.MAX_VALUE)
                            .addComponent(connLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 145, Short.MAX_VALUE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(closeButton)
                        .addGap(8, 8, 8)
                        .addComponent(connSelector, 0, 145, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(findSpritesButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(selectNoneButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reshapeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(selectAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(mergeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 74, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(storeClipsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {mergeButton, reshapeButton, selectAllButton, selectNoneButton});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {closeButton, openButton});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {findSpritesButton, storeClipsButton});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                    .addComponent(mergeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(selectNoneButton, javax.swing.GroupLayout.DEFAULT_SIZE, 39, Short.MAX_VALUE)
                    .addComponent(reshapeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(filterLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(openButton)
                    .addComponent(filterSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(connLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(closeButton)
                    .addComponent(connSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(findSpritesButton, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(storeClipsButton, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {mergeButton, reshapeButton, selectAllButton, selectNoneButton});

        jPanel1Layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {closeButton, openButton});

        javax.swing.GroupLayout sheetPanelLayout = new javax.swing.GroupLayout(sheetPanel);
        sheetPanel.setLayout(sheetPanelLayout);
        sheetPanelLayout.setHorizontalGroup(
            sheetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sheetPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(spriteSheetScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE)
        );
        sheetPanelLayout.setVerticalGroup(
            sheetPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, sheetPanelLayout.createSequentialGroup()
                .addComponent(spriteSheetScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 490, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        storedPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Clipped Sprites"));
        storedPanel.setFocusable(false);

        clippedList.setModel(new DefaultListModel());
        clippedList.setCellRenderer(new ClipCellRenderer());
        clippedList.setFocusable(false);
        clippedList.setLayoutOrientation(javax.swing.JList.HORIZONTAL_WRAP);
        clippedList.setVisibleRowCount(-1);
        jScrollPane2.setViewportView(clippedList);

        removeButton.setText("Remove");
        removeButton.setToolTipText("<html><div align=\"center\">Removes the selected clipped sprites. (Delete)</div></html>");
        removeButton.setEnabled(false);
        removeButton.setFocusable(false);
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        saveToButton.setText("Save To...");
        saveToButton.setToolTipText("<html><div align=\"center\">Saves all selected clipped sprites to a directory. (Ctrl+S)</div></html>");
        saveToButton.setEnabled(false);
        saveToButton.setFocusable(false);
        saveToButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveToButtonActionPerformed(evt);
            }
        });

        spriteDetailer.setFocusable(false);

        packButton.setText("Pack...");
        packButton.setToolTipText("<html><div align=\"center\">Packs the selected sprite clips into a new, dense sheet using the<br>selected packing method, and writes definition text file. (Ctrl+P)</div></html>");
        packButton.setEnabled(false);
        packButton.setFocusable(false);
        packButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packButtonActionPerformed(evt);
            }
        });

        packerLabel.setText("Packing method:");

        packerSelector.setModel(new DefaultComboBoxModel(spriteClipper.getAvailablePackers()));
        packerSelector.setFocusable(false);
        packerSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                packerSelectorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout storedPanelLayout = new javax.swing.GroupLayout(storedPanel);
        storedPanel.setLayout(storedPanelLayout);
        storedPanelLayout.setHorizontalGroup(
            storedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, storedPanelLayout.createSequentialGroup()
                .addComponent(packerSelector, 0, 156, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(packButton))
            .addGroup(storedPanelLayout.createSequentialGroup()
                .addComponent(packerLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(storedPanelLayout.createSequentialGroup()
                .addComponent(saveToButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
                .addComponent(removeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(spriteDetailer, javax.swing.GroupLayout.DEFAULT_SIZE, 216, Short.MAX_VALUE)
        );
        storedPanelLayout.setVerticalGroup(
            storedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(storedPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 316, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spriteDetailer, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(storedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(saveToButton)
                    .addComponent(removeButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(packerLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(storedPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(packerSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(packButton)))
        );

        storedPanelLayout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {removeButton, saveToButton});

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(sheetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(storedPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sheetPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(storedPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        try {
            spriteClipper.removeStoredClips(getSelectedStoredClips());
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
         try {
            spriteClipper.clearSpriteSheet();
         } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_closeButtonActionPerformed

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        try {
            JFileChooser chooser = new JFileChooser(currentDirectory);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                ".jpg, .gif, .png", "jpg", "gif", "png");
            chooser.setFileFilter(filter);
            int returnVal = chooser.showOpenDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File imageFile = chooser.getSelectedFile();
                currentDirectory = chooser.getCurrentDirectory();
                try {
                    spriteClipper.setSpriteSheet(imageFile);
                } catch (Exception e) {
                    exceptionDialog(e);
                }
            }
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void connSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connSelectorActionPerformed
        try {
            spriteClipper.setFinder((SpriteClipFinder) connSelector.getSelectedItem());
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_connSelectorActionPerformed

    private void filterSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterSelectorActionPerformed
        try {
            spriteClipper.setBackgroundFilter((BackgroundFilter) filterSelector.getSelectedItem());
        } catch (Exception e) {
            exceptionDialog(e);
        }
        
    }//GEN-LAST:event_filterSelectorActionPerformed

    private void findSpritesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findSpritesButtonActionPerformed
        try {
           spriteClipper.findSprites(); 
        }catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_findSpritesButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        setAllMarkers(true);
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void selectNoneButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectNoneButtonActionPerformed
        try {
            setAllMarkers(false);
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_selectNoneButtonActionPerformed

    private void mergeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mergeButtonActionPerformed
        try {
            JViewport viewport = spriteSheetScrollPane.getViewport();
            SpriteSheetPane ssPane = (SpriteSheetPane) viewport.getView();
            spriteClipper.mergeSprites(ssPane.getSelectedClips());
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_mergeButtonActionPerformed

    private void reshapeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reshapeButtonActionPerformed
        try {
            JViewport viewport = spriteSheetScrollPane.getViewport();
            SpriteSheetPane ssPane = (SpriteSheetPane) viewport.getView();
            ButtonModel selectedModel = rsAnchorGroup.getSelection();

            SpriteClip.AnchorType anchor;
            if (selectedModel == tlRadioButton.getModel()) {
                anchor = SpriteClip.AnchorType.TL;
            }else if (selectedModel == tcRadioButton.getModel()) {
                anchor = SpriteClip.AnchorType.TC;
            }else if (selectedModel == trRadioButton.getModel()) {
                anchor = SpriteClip.AnchorType.TR;
            }else if (selectedModel == clRadioButton.getModel()) {
                anchor = SpriteClip.AnchorType.CL;
            }else if (selectedModel == crRadioButton.getModel()) {
                anchor = SpriteClip.AnchorType.CR;
            }else if (selectedModel == blRadioButton.getModel()) {
                anchor = SpriteClip.AnchorType.BL;
            }else if (selectedModel == bcRadioButton.getModel()) {
                anchor = SpriteClip.AnchorType.BC;
            }else {
                anchor = SpriteClip.AnchorType.BR;
            }

            spriteClipper.reshapeSprites(ssPane.getSelectedClips(), anchor);
        } catch (Exception e) {
            exceptionDialog(e);
        }        
    }//GEN-LAST:event_reshapeButtonActionPerformed

    private void storeClipsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeClipsButtonActionPerformed
        try {
            SpriteSheetPane ssPane = (SpriteSheetPane)
                                             spriteSheetScrollPane.getViewport().getView();
            spriteClipper.storeClips(ssPane.getSelectedClips());
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_storeClipsButtonActionPerformed

    private void saveToButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveToButtonActionPerformed
        try {
            JFileChooser chooser = new JFileChooser(currentDirectory);
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int returnVal = chooser.showSaveDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File imageDirectory = chooser.getSelectedFile();
                currentDirectory = imageDirectory;
                spriteClipper.saveClips(getSelectedStoredClips(), imageDirectory, "png");
            }
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_saveToButtonActionPerformed

    private void packButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packButtonActionPerformed
        try {
            JFileChooser chooser = new JFileChooser(currentDirectory);
            chooser.setDialogTitle("Provide a name for sprite PNG pack");
            int returnVal = chooser.showSaveDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File packFile = chooser.getSelectedFile();
                currentDirectory = chooser.getCurrentDirectory();
                List<SpriteClip> clips = getSelectedStoredClips();
                if (clips == null) {
                    clips = spriteClipper.getStoredClips();
                }
                spriteClipper.pack(clips, packFile);  
            }
        } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_packButtonActionPerformed

    private void packerSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_packerSelectorActionPerformed
       try {
           spriteClipper.setSpritePacker((SpritePacker) packerSelector.getSelectedItem());
       } catch (Exception e) {
            exceptionDialog(e);
        }
    }//GEN-LAST:event_packerSelectorActionPerformed

    private void setAllMarkers(boolean bool) {
        JViewport viewport = spriteSheetScrollPane.getViewport();
        SpriteSheetPane ssPane = (SpriteSheetPane) viewport.getView();
        ssPane.setAllSelected(bool);
    }

    private void setSpriteControlsEnabled(boolean bool) {
        selectAllButton.setEnabled(bool);
        selectNoneButton.setEnabled(bool);
        mergeButton.setEnabled(bool);
        reshapeButton.setEnabled(bool);
        storeClipsButton.setEnabled(bool);
        bcRadioButton.setEnabled(bool);
        brRadioButton.setEnabled(bool);
        blRadioButton.setEnabled(bool);
        crRadioButton.setEnabled(bool);
        clRadioButton.setEnabled(bool);
        trRadioButton.setEnabled(bool);
        tcRadioButton.setEnabled(bool);
        tlRadioButton.setEnabled(bool);
    }

    private void setSaveControlsEnabled(boolean bool) {
        saveToButton.setEnabled(bool);
        removeButton.setEnabled(bool);
        packButton.setEnabled(bool);
    }

    private List<SpriteClip> getSelectedStoredClips() {
        Object[] selClipsArray = clippedList.getSelectedValues();
        List<SpriteClip> selClips = null;
            if (selClipsArray.length > 0) {
                selClips =  new ArrayList<SpriteClip>(selClipsArray.length);
                for (Object curClip : selClipsArray)
                selClips.add((SpriteClip) curClip);
            }
        return selClips;
    }

    public void keyPressed(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {

        int m = e.getModifiersEx();
        if ((m & e.CTRL_DOWN_MASK) == 0 && e.getKeyCode() != KeyEvent.VK_DELETE) {
            return;
        }

        ActionEvent a = null;
        switch(e.getKeyCode()) {
            case KeyEvent.VK_DELETE:
                removeButtonActionPerformed(a);     break;
            case KeyEvent.VK_C:
                storeClipsButtonActionPerformed(a); break;
            case KeyEvent.VK_R:
                reshapeButtonActionPerformed(a);    break;
            case KeyEvent.VK_M:
                mergeButtonActionPerformed(a);      break;
            case KeyEvent.VK_A:
                selectAllButtonActionPerformed(a);  break;
            case KeyEvent.VK_N:
                selectNoneButtonActionPerformed(a); break;
            case KeyEvent.VK_W:
                closeButtonActionPerformed(a);      break;
            case KeyEvent.VK_O:
                openButtonActionPerformed(a);       break;
            case KeyEvent.VK_S:
                saveToButtonActionPerformed(a);     break;
            case KeyEvent.VK_F:
                findSpritesButtonActionPerformed(a);break;
            case KeyEvent.VK_P:
                packButtonActionPerformed(a);       break;
            default:  
        }
    }

    private void exceptionDialog(Exception e){
        e.printStackTrace();
        JOptionPane.showMessageDialog(  this,
                                        e.toString(),
                                        "Oops!",
                                        JOptionPane.ERROR_MESSAGE);
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        try {
	    // Set System L&F
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
           System.out.println(e);
        }
        catch (ClassNotFoundException e) {
           System.out.println(e);
        }
        catch (InstantiationException e) {
           System.out.println(e);
        }
        catch (IllegalAccessException e) {
           System.out.println(e);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                SpriteClipperGUI gui = new SpriteClipperGUI();
                gui.setFocusable(true);
                gui.setVisible(true);
            }
        });
    }

    private SpriteClipper spriteClipper = new SpriteClipper();
    private File currentDirectory = null;

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton bcRadioButton;
    private javax.swing.JRadioButton blRadioButton;
    private javax.swing.JRadioButton brRadioButton;
    private javax.swing.JRadioButton clRadioButton;
    private javax.swing.JList clippedList;
    private javax.swing.JButton closeButton;
    private javax.swing.JLabel connLabel;
    private javax.swing.JComboBox connSelector;
    private javax.swing.JRadioButton crRadioButton;
    private javax.swing.JLabel filterLabel;
    private javax.swing.JComboBox filterSelector;
    private javax.swing.JButton findSpritesButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JButton mergeButton;
    private javax.swing.JButton openButton;
    private javax.swing.JButton packButton;
    private javax.swing.JLabel packerLabel;
    private javax.swing.JComboBox packerSelector;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton reshapeButton;
    private javax.swing.ButtonGroup rsAnchorGroup;
    private javax.swing.JButton saveToButton;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton selectNoneButton;
    private javax.swing.JPanel sheetPanel;
    private com.fluffynukeit.SpriteClipper.GUI.SpriteDetailer spriteDetailer;
    private javax.swing.JScrollPane spriteSheetScrollPane;
    private javax.swing.JButton storeClipsButton;
    private javax.swing.JPanel storedPanel;
    private javax.swing.JRadioButton tcRadioButton;
    private javax.swing.JRadioButton tlRadioButton;
    private javax.swing.JRadioButton trRadioButton;
    // End of variables declaration//GEN-END:variables

}
