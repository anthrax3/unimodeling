package info.sarihh.unimodeling.gui;

import info.sarihh.unimodeling.utility.DrawConnection;
import info.sarihh.unimodeling.utility.DrawDelimiter;
import info.sarihh.unimodeling.utility.DrawEdge;
import info.sarihh.unimodeling.utility.DrawElement;
import info.sarihh.unimodeling.utility.DrawRfid;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * This class is a panel that contains a designer layer. Is has a context menu
 * and a dialog for customization.
 * Author: Sari Haj Hussein
 */
public class DrawLayer extends javax.swing.JPanel {

    public static final int NONE = -1;
    public static final int DELIMITER = 0;
    public static final int VERTEX = 1;
    public static final int EDGE = 2;
    public static final int CONNECTION = 3;
    public static final int RFID = 4;
    public static final int MOVE = 5;
    public static final int RESIZE = 6;
    public static final int DIRECTION = 7;
    public static final int ROUTE = 8;
    private int usedTool;
    private int previousTool;
    private DrawElement usedElement;
    private ArrayList<DrawElement> elements;
    private Dimension defaultSize;
    private DrawElement invisibleRfid;
    private DrawPanel drawPanel;
    private Point contextMenuPoint;
    private List<DirectionLine> lines;

    /**
     * Creates a new drawing layer.
     * @param d size of the layer
     * @param drawPanel parent tab
     */
    public DrawLayer(Dimension d, DrawPanel drawPanel) {
        this.drawPanel = drawPanel;
        initComponents();
        setSize(d);
        setPreferredSize(d);
        setFocusable(true);
        grabFocus();
        elements = new ArrayList<>();
        usedTool = DELIMITER;
        lines = new ArrayList<>();
        defaultSize = new Dimension(getSize());
        invisibleRfid = new DrawRfid(new Point(0, 0), new Point(1, 1));
    }

    /**
     * Sets the used tool based on toggle buttons.
     * @param tool tool
     */
    public void setTool(int tool) {
        usedTool = tool;
    }

    /**
     * Resize the layer by given ratios
     * @param ratioX horizontal ratio
     * @param ratioY vertical ratio
     */
    public void resize(float ratioX, float ratioY) {
        ///@todo: rewrite it completely
        float cw = getSize().width;
        float ch = getSize().height;
        float w = defaultSize.width * ratioX;
        float h = defaultSize.height * ratioY;
        setSize(Math.round(w), Math.round(h));
        setPreferredSize(getSize());
        for (DrawElement elem : elements) {
            elem.resize(w / cw, h / ch);
        }
        repaint();
    }

    /**
     * Returns the element where right-click occurred. The order depends on the 
     * element's type and on creation order.
     * @return Clicked element
     */
    private DrawElement getClickedObject(Point p) {
        if (p == null) {
            return null;
        }
        DrawElement click = null;
        for (DrawElement e : elements) {
            if (e.hasPoint(p)) {
                click = e;
            }
        }
        return click;
    }

    /**
     * Creates a panel from DirectionLine objects
     * @param delims delimiter list
     * @return direction selection panel
     */
    private JPanel directionsPanel(Object[] delims) {
        lines.clear();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(new JLabel("Choose directions"));
        for (int i = 0; i < Array.getLength(delims); ++i) {
            DirectionLine line = new DirectionLine(delims, i);
            lines.add(line);
            panel.add(line);
        }
        return panel;
    }

    /**
     * Creates a panel from DirectionLine objects
     * @param delims delimiter list
     * @param values connection matrix
     * @return direction selection panel
     */
    private JPanel directionsPanel(Object[] delims, Boolean[][] values) {
        lines.clear();
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        add(new JLabel("Choose directions"));
        for (int i = 0; i < Array.getLength(delims); ++i) {
            DirectionLine line = new DirectionLine(delims, i, values[i]);
            lines.add(line);
            panel.add(line);
        }
        return panel;
    }

    /**
     * This class represents a line on a direction selection panel. This line
     * decides which delimiters are connected to this one. It uses checkboxes
     * to indicate directions.
     * @todo make this into table
     */
    private class DirectionLine extends JPanel {

        public List<JCheckBox> checkBoxes;
        private JLabel heading;

        /**
         * Creates a lines of checkboxes.
         * @param delims delimiter list
         * @param id delimiter index
         */
        public DirectionLine(Object[] delims, int id) {
            checkBoxes = new ArrayList<>();
            setLayout(new FlowLayout());
            heading = new JLabel(delims[id] + " --> ");
            add(heading);
            for (int i = 0; i < Array.getLength(delims); ++i) {
                JCheckBox box = new JCheckBox(delims[i].toString());
                add(box);
                checkBoxes.add(box);
            }
        }

        /**
         * Creates a lines of checkboxes.
         * @param delims delimiter list
         * @param id delimiter index
         * @param values connections
         */
        public DirectionLine(Object[] delims, int id, Boolean[] values) {
            checkBoxes = new ArrayList<>();
            setLayout(new FlowLayout());
            heading = new JLabel(delims[id] + " --> ");
            add(heading);
            for (int i = 0; i < Array.getLength(delims); ++i) {
                JCheckBox box = new JCheckBox(delims[i].toString());
                box.setSelected(values[i]);
                add(box);
                checkBoxes.add(box);
            }
        }

        /**
         * Gets the selected checkboxes as list.
         * @return selected directions
         */
        public List<Boolean> getSelected() {
            List<Boolean> result = new ArrayList<>();
            for (JCheckBox cb : checkBoxes) {
                result.add(cb.isSelected());
            }
            return result;
        }
    }

    /**
     * Adds a new DrawElement to the layer.
     * @param e new element
     */
    public void addElement(DrawElement e) {
        elements.add(e);
    }

    /**
     * Adds a list of DrawElement to the layer.
     * @param e new element
     */
    public void addElements(List<DrawElement> e) {
        elements.addAll(e);
    }

    /**
     * Clears all the elements from the layer.
     */
    public void clearElements() {
        elements.clear();
    }

    /**
     * Gets the specified element from the layer. Elements are searched by 
     * OrientGraph ID
     * @param id OrientGraph ID
     * @return element
     */
    public DrawElement getElement(Object id) {
        for (DrawElement e : elements) {
            if (e.getBpID() != null && e.getBpID().equals(id)) {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets the specified element from the layer. Elements are searched by 
     * midGraph ID
     * @param id midGraph ID
     * @return element
     */
    public DrawElement getElement(int id) {
        for (DrawElement e : elements) {
            if (e.getMID() == id) {
                return e;
            }
        }
        return null;
    }

    /**
     * Gets the element list
     * @return elements
     */
    public List<DrawElement> getElements() {
        return elements;
    }

    /**
     * Checks if any element is selected.
     * @return if elements selected
     */
    private boolean checkRoute() {
        boolean b = false;
        for (DrawElement e : elements) {
            b |= e.isSelected();
        }
        return b;
    }

    private List<DrawElement> getRoute() {
        List<DrawElement> route = new ArrayList<>();
        for (DrawElement e : elements) {
            if (e.getType() == VERTEX && e.isSelected()) {
                route.add(e);
            }
        }
        return route;
    }

    /**
     * Overriden paintComponent method to draw elements on the layer.
     * @param g Graphics object
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2D = (Graphics2D) g;
        if (usedElement != null) {
            usedElement.draw(g2D);
        }
        if (!elements.isEmpty()) {
            for (DrawElement e : elements) {
                e.draw(g2D);
            }
        }
        g2D.setStroke(new BasicStroke(4));
        g2D.setColor(Color.black);
        if (usedTool == RFID) {
            invisibleRfid.draw(g2D);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        drawContextMenu = new javax.swing.JPopupMenu();
        renameMenuItem = new javax.swing.JMenuItem();
        customizeMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        directionsMenuItem = new javax.swing.JMenuItem();
        observabilityMenuItem = new javax.swing.JMenuItem();
        estimateMenuItem = new javax.swing.JMenuItem();
        customizePanel = new javax.swing.JPanel();
        propertyNameLabel = new javax.swing.JLabel();
        propertyNameTextField = new javax.swing.JTextField();
        propertyValueLabel = new javax.swing.JLabel();
        propertyValueTextField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        removeallButton = new javax.swing.JButton();
        propertiesTableScrollPane = new javax.swing.JScrollPane();
        propertiesTable = new javax.swing.JTable();
        cwTableScrollPane = new javax.swing.JScrollPane();
        cwTable = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();

        drawContextMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
            }
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                drawContextMenuPopupMenuWillBecomeVisible(evt);
            }
        });

        renameMenuItem.setText("Rename");
        renameMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameMenuItemActionPerformed(evt);
            }
        });
        drawContextMenu.add(renameMenuItem);

        customizeMenuItem.setText("Extend");
        customizeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                customizeMenuItemActionPerformed(evt);
            }
        });
        drawContextMenu.add(customizeMenuItem);

        deleteMenuItem.setText("Delete");
        deleteMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteMenuItemActionPerformed(evt);
            }
        });
        drawContextMenu.add(deleteMenuItem);

        directionsMenuItem.setText("Directions");
        directionsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                directionsMenuItemActionPerformed(evt);
            }
        });
        drawContextMenu.add(directionsMenuItem);

        observabilityMenuItem.setText("Observability");
        observabilityMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                observabilityMenuItemActionPerformed(evt);
            }
        });
        drawContextMenu.add(observabilityMenuItem);

        estimateMenuItem.setText("Static BP Estimate");
        estimateMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                estimateMenuItemActionPerformed(evt);
            }
        });
        drawContextMenu.add(estimateMenuItem);

        propertyNameLabel.setText("Property name:");

        propertyNameTextField.setColumns(20);

        propertyValueLabel.setText("Property value:");

        propertyValueTextField.setColumns(20);

        addButton.setText("+");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        removeButton.setText("-");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });

        removeallButton.setText("X");
        removeallButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeallButtonActionPerformed(evt);
            }
        });

        propertiesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Key", "Value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        propertiesTableScrollPane.setViewportView(propertiesTable);

        cwTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "RFID", "Weight"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        cwTableScrollPane.setViewportView(cwTable);

        jLabel1.setText("Coverage weights:");

        javax.swing.GroupLayout customizePanelLayout = new javax.swing.GroupLayout(customizePanel);
        customizePanel.setLayout(customizePanelLayout);
        customizePanelLayout.setHorizontalGroup(
            customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizePanelLayout.createSequentialGroup()
                .addGroup(customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(propertyNameLabel)
                    .addGroup(customizePanelLayout.createSequentialGroup()
                        .addGap(1, 1, 1)
                        .addComponent(propertyValueLabel)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(propertyNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE)
                    .addComponent(propertyValueTextField)))
            .addGroup(customizePanelLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1)
                    .addGroup(customizePanelLayout.createSequentialGroup()
                        .addComponent(propertiesTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(removeallButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(addButton)))
                    .addComponent(cwTableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
        customizePanelLayout.setVerticalGroup(
            customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(customizePanelLayout.createSequentialGroup()
                .addGroup(customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(customizePanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(propertyNameLabel))
                    .addComponent(propertyNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(customizePanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(propertyValueLabel))
                    .addComponent(propertyValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(customizePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(customizePanelLayout.createSequentialGroup()
                        .addComponent(addButton)
                        .addGap(0, 0, 0)
                        .addComponent(removeButton)
                        .addGap(0, 0, 0)
                        .addComponent(removeallButton))
                    .addComponent(propertiesTableScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cwTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
        );

        setComponentPopupMenu(drawContextMenu);
        setOpaque(false);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 355, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 141, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        requestFocusInWindow();
        if (evt.getButton() == MouseEvent.BUTTON1) {
            switch (usedTool) {
                case DELIMITER:
                    usedElement = new DrawDelimiter(evt.getPoint(), evt.getPoint());
                    break;
                case CONNECTION: {
                    int selectIndex = -1;
                    DrawElement startPoint = null;
                    for (DrawElement e : elements) {
                        if (e.hasPoint(evt.getPoint())) {
                            selectIndex = elements.indexOf(e);
                            e.setSelected(true);
                            startPoint = e;
                            break;
                        }
                    }
                    usedElement = new DrawConnection(evt.getPoint(), evt.getPoint());
                    usedElement.setStartError(selectIndex == -1);
                    break;
                }
                case VERTEX:
                    break;
                case EDGE: {
                    int selectIndex = -1;
                    for (DrawElement e : elements) {
                        if (e.hasPoint(evt.getPoint())) {
                            selectIndex = elements.indexOf(e);
                        }
                    }
                    if (selectIndex > -1) {
                        DrawElement startPoint = elements.get(selectIndex);
                        DrawEdge edge = new DrawEdge(
                                startPoint.getCentralPoint(),
                                evt.getPoint());
                        edge.setStartPoint(startPoint);
                        usedElement = edge;
                    } else {
                        usedElement = new DrawEdge(evt.getPoint(), evt.getPoint());
                        usedElement.setStartError(true);
                    }
                    break;
                }
                case RFID: {
                    usedElement = invisibleRfid;
                    break;
                }
                case MOVE: {
                    usedElement = getClickedObject(evt.getPoint());
                    if (usedElement != null && usedElement.getType() != EDGE) {
                        usedElement.setStartMovePoint(evt.getPoint());
                    }
                }
                break;
                case RESIZE: {
                    usedElement = getClickedObject(evt.getPoint());
                    if (usedElement != null && usedElement.getType() != EDGE) {
                        usedElement.resizePart(evt.getPoint());
                    }
                    break;
                }
                default:
                    break;
            }
            repaint();
        }
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        if (evt.getButton() == MouseEvent.BUTTON1) {
            if (usedElement != null && (usedElement.getStartError()
                    || usedElement.getEndError() || usedElement.isNullSize())) {
                usedElement = null;
                repaint();
            } else {
                switch (usedTool) {
                    case DELIMITER: {
                        String name = JOptionPane.showInputDialog(
                                "Semantic location's name?",
                                "delim" + DrawDelimiter.counter++);
                        if (name == null) { //cancel pressed
                            usedElement = null;
                            repaint();
                            return;
                        }
                        usedElement.setName(name);
                        drawPanel.addMVertex(usedElement);
                        break;
                    }
                    case CONNECTION: {
                        List<String> names = new ArrayList<>();
                        List<DrawElement> elems = new ArrayList<>();
                        for (DrawElement e : elements) {
                            if (e.isSelected()) {
                                names.add(e.getName());
                                elems.add(e);
                                usedElement.addConnection(e);
                                e.setSelected(false);
                            }
                        }
                        String name = "{";
                        for (String s : names) {
                            name += s + "|";
                        }
                        String iName = JOptionPane.showInputDialog(
                                "Connection point's name?",
                                name.substring(0, name.length() - 1) + "}");
                        if (iName == null) { //cancel pressed
                            usedElement = null;
                            repaint();
                            return;
                        }
                        int counter = 1;
                        for (DrawElement e : elements) {
                            if (e.getType() == CONNECTION && e.getName().equals(iName)) {
                                ++counter;
                                ((DrawConnection) e).setWriteName(true);
                            }
                        }
                        usedElement.setName(iName);
                        ((DrawConnection) usedElement).setNum(counter);
                        if (counter > 1) {
                            ((DrawConnection) usedElement).setWriteName(true);
                        }
                        int s = names.size();
                        Boolean endpoints[][] = new Boolean[s][s];
                        for (int i = 0; i < s; ++i) {
                            for (int j = 0; j < s; ++j) {
                                if (elems.get(i).hasPoint(usedElement.getPoint1())) {
                                    endpoints[i][j] = elems.get(j).hasPoint(usedElement.getPoint2());
                                } else {
                                    endpoints[i][j] = false;
                                }
                            }
                        }
                        int result = JOptionPane.showConfirmDialog(
                                drawPanel,
                                directionsPanel(names.toArray(), endpoints),
                                "Choose directions",
                                JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE);
                        if (result == JOptionPane.OK_OPTION) {
                            Boolean connections[][] = new Boolean[s][s];
                            for (int i = 0; i < s; ++i) {
                                connections[i] = lines.get(i).getSelected().toArray(connections[i]);
                            }
                            boolean anyConnect = false;
                            for (int i = 0; i < s * s; ++i) {
                                anyConnect |= connections[i / s][i % s];
                            }
                            if (!anyConnect) {
                                usedElement = null;
                                repaint();
                                return;
                            }
                            usedElement.setConnectionMatrix(connections);
                            usedElement.setID(DrawConnection.counter++);
                            elements.addAll(drawPanel.addMEdge(usedElement));
                        } else {
                            usedElement = null;
                            repaint();
                            return;
                        }

                    }
                    case VERTEX:
                        break;
                    case EDGE:
                        break;
                    case RFID: {
                        DrawElement selected = null;
                        List<DrawElement> conns = new ArrayList<>();
                        for (DrawElement e : elements) {
                            if (usedElement.isColliding(e)) {
                                if (e.getType() == DELIMITER) {
                                    conns.add(e);
                                }
                                selected = e;
                            }
                        }
                        if (selected.getType() == DELIMITER && conns.size() > 1) {
                            String dNames[] = new String[conns.size()];
                            for (int i = 0; i < conns.size(); ++i) {
                                dNames[i] = conns.get(i).getName();
                            }
                            String result = (String) JOptionPane.showInputDialog(drawPanel,
                                    "In which location you would like to place the RFID reader?",
                                    "RFID placement",
                                    JOptionPane.PLAIN_MESSAGE, null,
                                    dNames, selected.getName());
                            if (result == null) {
                                break;
                            }
                            for (int i = 0; i < conns.size(); ++i) {
                                if (dNames[i].equals(result)) {
                                    selected = conns.get(i);
                                }
                            }
                            usedElement.addConnection(selected);
                            ((DrawRfid) usedElement).setRatio(1.0);
                        }
                        if (selected.getType() == CONNECTION) {
                            usedElement.addConnection(selected);
                            int o = ((DrawConnection) selected).getOrientation();
                            double ratio;
                            if (o == DrawConnection.VERTICAL) {
                                ratio = (selected.getCentralPoint().getY()
                                        - usedElement.getCentralPoint().getY()) / 16;
                            } else {
                                ratio = (selected.getCentralPoint().getX()
                                        - usedElement.getCentralPoint().getX()) / 16;
                            }
                            ((DrawRfid) usedElement).setRatio(ratio);
                            ((DrawConnection) selected).getRfids().add((DrawRfid) usedElement);
                            ((DrawConnection) selected).sortRfids();
                        }
                        usedElement.addConnection(selected);

                        String name = JOptionPane.showInputDialog(
                                "RFID reader name?",
                                "r" + DrawRfid.counter++);

                        if (name != null) {
                            usedElement.setName(name);
                            invisibleRfid = new DrawRfid(new Point(0, 0), new Point(1, 1));
                            drawPanel.addMRfid(usedElement);
                        } else {
                            DrawRfid.counter--;
                        }
                        break;
                    }
                    case MOVE:
                    case RESIZE:
                        if (usedElement != null) {
                            drawPanel.changeMComponent(usedElement, usedElement.getName());
                        }
                        break;
                    case ROUTE:
                        if (drawPanel.getSelectedDesigner() == DrawPanel.RFID) {
                            getClickedObject(evt.getPoint()).setSelected(
                                    !getClickedObject(evt.getPoint()).isSelected());
                        }
                        break;
                    default:
                        break;
                }
                if (usedElement != null) {
                    elements.add(usedElement);
                    Collections.sort(elements);
                }
            }
        }
        repaint();
    }//GEN-LAST:event_formMouseReleased

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        if ((evt.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) {
            if (usedElement == null) {
                return;
            }
            boolean error = false;
            Point point1 = usedElement.getPoint1();
            Point point2 = null;
            switch (usedTool) {
                case DELIMITER:
                    for (DrawElement e : elements) {
                        error |= usedElement.isColliding(e);
                    }
                    point2 = evt.getPoint();
                    break;
                case CONNECTION: {
                    int counter = 0;
                    for (DrawElement e : elements) {
                        if (usedElement.isColliding(e) && e.getType() == DELIMITER) {
                            e.setSelected(true);
                            ++counter;
                        } else {
                            e.setSelected(false);
                        }
                    }
                    point2 = evt.getPoint();
                    if (counter < 2) {
                        error = true;
                    }
                    break;
                }
                case VERTEX:
                    break;
                case EDGE: {
                    int selectIndex = -1;
                    for (DrawElement e : elements) {
                        if (e.hasPoint(evt.getPoint()) && e.getType() != EDGE) {
                            selectIndex = elements.indexOf(e);
                        }
                    }
                    if (selectIndex > -1) {
                        point2 = elements.get(selectIndex).getCentralPoint();
                        ((DrawEdge) usedElement).setEndPoint(elements.get(selectIndex));
                        error = false;
                    } else {
                        point2 = evt.getPoint();
                        error = true;
                    }
                    break;
                }
                case RFID: {
                    point2 = point1;
                    break;
                }
                case MOVE: {
                    if (usedElement != null && usedElement.getType() != EDGE) {
                        usedElement.move(evt.getPoint());
                        point1 = usedElement.getPoint1();
                        point2 = usedElement.getPoint2();
                    } else {
                        return;
                    }
                    break;

                }
                case RESIZE: {
                    usedElement.resize(evt.getPoint());
                    point1 = usedElement.getPoint1();
                    point2 = usedElement.getPoint2();
                    break;
                }
                default:
                    break;
            }
            usedElement.setEndError(error);
            usedElement.setPoint1(point1);
            usedElement.setPoint2(point2);
        }
        repaint();
    }//GEN-LAST:event_formMouseDragged

    private void renameMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameMenuItemActionPerformed
        DrawElement rightClick = getClickedObject(contextMenuPoint);
        String oldName = "";
        if (rightClick != null) {
            oldName = rightClick.getName();
            String name = JOptionPane.showInputDialog(
                    "Elements's new name?",
                    oldName);
            if (name != null) {
                rightClick.setName(name);
                drawPanel.changeMComponent(rightClick, oldName);
            }
        }
        repaint();
    }//GEN-LAST:event_renameMenuItemActionPerformed

    private void customizeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_customizeMenuItemActionPerformed
        DrawElement rightClick = getClickedObject(contextMenuPoint);
        if (rightClick != null) {
            Map<String, Object> properties = drawPanel.getProperties(rightClick);
            TableModel propModel = propertiesTable.getModel();
            int r = propModel.getRowCount();
            for (int i = 0; i < r; ++i) {
                ((DefaultTableModel) propModel).removeRow(0);
            }
            for (String s : properties.keySet()) {
                ((DefaultTableModel) propModel).addRow(new Object[]{s, properties.get(s)});
            }

            TableModel cwModel = cwTable.getModel();
            r = cwTable.getRowCount();
            for (int i = 0; i < r; ++i) {
                ((DefaultTableModel) cwModel).removeRow(0);
            }
            for (String s : drawPanel.getCoverageWeights(rightClick)) {
                String data[] = s.split("->");
                ((DefaultTableModel) cwModel).addRow(data);
            }

            int returnValue = JOptionPane.showConfirmDialog(drawPanel.getRootPane(), customizePanel,
                    "Customize " + rightClick.getName(), JOptionPane.OK_CANCEL_OPTION);

            switch (returnValue) {
                case JOptionPane.OK_OPTION:
                    r = propertiesTable.getRowCount();
                    properties.clear();
                    for (int i = 0; i < r; ++i) {
                        properties.put((String) propertiesTable.getValueAt(i, 0),
                                propertiesTable.getValueAt(i, 1));
                    }
                    drawPanel.setProperties(rightClick, properties);

                    r = cwTable.getRowCount();
                    Set<String> result = new HashSet<>();
                    for (int i = 0; i < r; ++i) {
                        result.add(cwTable.getValueAt(i, 0) + "->" + cwTable.getValueAt(i, 1));
                    }
                    drawPanel.setCoverageWeights(rightClick, result);
                    break;
                case JOptionPane.CANCEL_OPTION:
            }
        }
        repaint();
    }//GEN-LAST:event_customizeMenuItemActionPerformed

    private void deleteMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteMenuItemActionPerformed
        DrawElement rightClick = getClickedObject(contextMenuPoint);
        if (rightClick != null) {
            List<DrawElement> toRemove = new ArrayList<>();
            toRemove.add(rightClick);
            switch (rightClick.getType()) {
                case DELIMITER:
                    for (DrawElement e : rightClick.getConnections()) {
                        if (e.getType() == RFID || e.getType() == CONNECTION
                                || e.getType() == DIRECTION) {
                            toRemove.add(e);
                        }
                    }
                    drawPanel.deleteMVertex(rightClick);
                    break;
                case VERTEX:
                    break;
                case CONNECTION:
                    for (DrawElement e : elements) {
                        if (e.getType() == RFID
                                && e.getConnections().get(0).getType() == CONNECTION
                                && e.getConnections().get(0) == rightClick) {
                            toRemove.add(e);
                        }
                    }
                    toRemove.addAll(drawPanel.deleteMEdge(rightClick));
                    break;
                case EDGE:
                    break;
                case RFID:
                    drawPanel.deleteMRfid(rightClick);
                    break;
            }
            for (DrawElement e : toRemove) {
                elements.remove(e);
            }
        }
        usedElement = null;
        repaint();
    }//GEN-LAST:event_deleteMenuItemActionPerformed

    private void drawContextMenuPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_drawContextMenuPopupMenuWillBecomeVisible
        contextMenuPoint = this.getMousePosition();
        renameMenuItem.setEnabled(true);
        customizeMenuItem.setEnabled(true);
        deleteMenuItem.setEnabled(true);
        directionsMenuItem.setEnabled(true);
        if (drawPanel.getSelectedDesigner() == DrawPanel.GRAPH
                || drawPanel.getSelectedDesigner() == DrawPanel.ORIGINAL) {
            customizeMenuItem.setEnabled(false);
            observabilityMenuItem.setEnabled(false);
            estimateMenuItem.setEnabled(false);
        } else {
            if (getClickedObject(contextMenuPoint) != null && getClickedObject(contextMenuPoint).isSelected()) {
                observabilityMenuItem.setEnabled(true);
            } else {
                observabilityMenuItem.setEnabled(false);
            }
        }
        if (getClickedObject(contextMenuPoint) == null) {
            renameMenuItem.setEnabled(false);
            customizeMenuItem.setEnabled(false);
            deleteMenuItem.setEnabled(false);
            directionsMenuItem.setEnabled(false);
        } else if (getClickedObject(contextMenuPoint).getType() != CONNECTION) {
            directionsMenuItem.setEnabled(false);
        }
    }//GEN-LAST:event_drawContextMenuPopupMenuWillBecomeVisible

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        switch (usedTool) {
            case RFID: {
                Point m = getMousePosition();
                DrawElement selected = null;
                List<DrawElement> selLoc = new ArrayList<>();
                for (DrawElement e : elements) {
                    if (invisibleRfid.isColliding(e)) {
                        if (e.getType() == DELIMITER) {
                            selLoc.add(e);
                        }
                        selected = e;
                    }
                }
                for (DrawElement e : elements) {
                    e.setSelected(false);
                }
                if (selected != null) {
                    if (selected.getType() == CONNECTION) {
                    } else if (!selLoc.isEmpty()) {
                        for (DrawElement el : selLoc) {
                            el.setSelected(true);
                        }
                    }
                    selected.setSelected(true);
                    invisibleRfid.setStartError(false);
                } else {
                    invisibleRfid.setStartError(true);
                }
                if (m != null) {
                    invisibleRfid.setVisible(true);
                    invisibleRfid.setPoint1(new Point(m.x - 15, m.y - 15));
                    invisibleRfid.setPoint2(new Point(m.x + 15, m.y + 15));
                } else {
                    invisibleRfid.setVisible(false);
                }
                repaint();
                break;
            }
            case MOVE: {
                if (elements.isEmpty()) {
                    System.out.print("empty");
                }
                boolean touch = false;
                for (DrawElement e : elements) {
                    if (e.hasPoint(evt.getPoint()) && e.getType() != EDGE) {
                        setCursor(new Cursor(Cursor.MOVE_CURSOR));
                        touch = true;
                        break;
                    }
                }
                if (!touch) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
                break;
            }
            case RESIZE: {
                boolean touch = false;
                for (DrawElement e : elements) {
                    if (e.hasPoint(evt.getPoint())) {
                        setCursor(e.getCursor(evt.getPoint()));
                        touch = true;
                        break;
                    }
                }
                if (!touch) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
            default:
                break;
        }
    }//GEN-LAST:event_formMouseMoved

    private void directionsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_directionsMenuItemActionPerformed
        DrawElement rightClick = getClickedObject(contextMenuPoint);
        if (rightClick != null) {
            List<String> names = new ArrayList<>();
            for (DrawElement e : rightClick.getConnections()) {
                names.add(e.getName());
            }
            int result = JOptionPane.showConfirmDialog(
                    drawPanel,
                    directionsPanel(names.toArray(), rightClick.getConnectionMatrix()),
                    "Choose directions",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            int s = names.size();
            if (result == JOptionPane.OK_OPTION) {
                Boolean connections[][] = new Boolean[s][s];
                for (int i = 0; i < s; ++i) {
                    connections[i] = lines.get(i).getSelected().toArray(connections[i]);
                }
                boolean anyConnect = false;
                for (int i = 0; i < s * s; ++i) {
                    anyConnect |= connections[i / s][i % s];
                }
                if (!anyConnect) {
                    elements.remove(rightClick);
                    repaint();
                    return;
                }
                elements.removeAll(drawPanel.deleteMEdge(rightClick));
                rightClick.setConnectionMatrix(connections);
                elements.addAll(drawPanel.addMEdge(rightClick));
            } else {
                usedElement = null;
            }
            repaint();
        }
    }//GEN-LAST:event_directionsMenuItemActionPerformed

    /**
     * Key adapter. Only CTRL key is observed. If it is pressed route selection
     * is active.
     * @param evt key event 
     */
    private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_CONTROL) {
            previousTool = (usedTool == ROUTE) ? previousTool : usedTool;
            usedTool = ROUTE;
        }
    }//GEN-LAST:event_formKeyPressed

    private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
        if (evt.getKeyCode() == KeyEvent.VK_CONTROL) {
            usedTool = previousTool;
        }
    }//GEN-LAST:event_formKeyReleased

    private void observabilityMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_observabilityMenuItemActionPerformed
        String routeLocations[] = new String[getRoute().size()];
        int i = 0;
        for (DrawElement e : getRoute()) {
            routeLocations[i++] = drawPanel.getComponentName(e);
        }
        JOptionPane.showMessageDialog(drawPanel,
                "R = (" + routeLocations[0] + "..." + routeLocations[routeLocations.length - 1] + ")\n"
                + "obs(R) = " + truncate(drawPanel.getObs(routeLocations), 5) + " bits\n"
                + "bounds(R) = [0, " + truncate(drawPanel.getUpperBound(routeLocations), 5) + "]",
                "UniModeling",
                JOptionPane.INFORMATION_MESSAGE);
        for (DrawElement e : elements) {
            e.setSelected(false);
        }
    }//GEN-LAST:event_observabilityMenuItemActionPerformed

    /** This method truncates the specified double value to the specified number
     * of decimal places. */
    private double truncate(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(d);
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    private void estimateMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_estimateMenuItemActionPerformed
        for (DrawElement e : elements) {
            if (e.getType() == VERTEX) {
                e.addProperties("BP: "
                        + Round(drawPanel.getStaticBPEstimate(drawPanel.getComponentName(e)), 2) + "_");
            }
        }
        repaint();
    }//GEN-LAST:event_estimateMenuItemActionPerformed

    public static double Round(double number, int decimals) {
        double mod = Math.pow(10.0, decimals);
        return Math.round(number * mod) / mod;
    }

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String key;
        String value;
        try {
            key = propertyNameTextField.getText();
            value = propertyValueTextField.getText();
        } catch (NullPointerException npe) {
            return;
        }
        ((DefaultTableModel) propertiesTable.getModel()).addRow(
                new Object[]{key, value});
    }//GEN-LAST:event_addButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        int row = propertiesTable.getSelectedRow();
        if (row >= 0) {
            ((DefaultTableModel) propertiesTable.getModel()).removeRow(row);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void removeallButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeallButtonActionPerformed
        int row = propertiesTable.getRowCount();
        for (int i = 0; i < row; ++i) {
            ((DefaultTableModel) propertiesTable.getModel()).removeRow(0);
        }
    }//GEN-LAST:event_removeallButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JMenuItem customizeMenuItem;
    private javax.swing.JPanel customizePanel;
    private javax.swing.JTable cwTable;
    private javax.swing.JScrollPane cwTableScrollPane;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem directionsMenuItem;
    private javax.swing.JPopupMenu drawContextMenu;
    private javax.swing.JMenuItem estimateMenuItem;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuItem observabilityMenuItem;
    private javax.swing.JTable propertiesTable;
    private javax.swing.JScrollPane propertiesTableScrollPane;
    private javax.swing.JLabel propertyNameLabel;
    private javax.swing.JTextField propertyNameTextField;
    private javax.swing.JLabel propertyValueLabel;
    private javax.swing.JTextField propertyValueTextField;
    private javax.swing.JButton removeButton;
    private javax.swing.JButton removeallButton;
    private javax.swing.JMenuItem renameMenuItem;
    // End of variables declaration//GEN-END:variables
}
