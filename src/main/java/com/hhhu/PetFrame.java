package com.hhhu;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PetFrame extends JFrame {
    private enum PetState {
        IDLE, SLEEPING, WALKING
    }
    private PetState currentState = PetState.IDLE;
    private PetState lastState = PetState.IDLE;
    private Timer animationTimer;
    private final Random random = new Random();
    private Rectangle screenBounds;
    private int currentFrame = 0;
    private final int moveSpeed = 3;
    private final List<BufferedImage> idleFrames = new ArrayList<>();
    private final List<BufferedImage> happyFrames = new ArrayList<>();
    private final List<BufferedImage> walkRightFrames = new ArrayList<>();
    private final List<BufferedImage> walkLeftFrames = new ArrayList<>();
    private final List<BufferedImage> sleepFrames = new ArrayList<>();

    private Point dragOffset = new Point();
    private boolean isDragging = false;
    private boolean isWalk = false;

    public PetFrame() {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.awt.graphics.UseQuartz", "true"); // 更好的图形渲染
            System.setProperty("apple.awt.antialiasing", "on"); // 开启抗锯齿
        }

        initWindow();
        loadPetImages();
        initAnimation();
        setupMouseListeners();
        setupSystemTray();
    }

    private void initWindow() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setSize(100, 100);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        screenBounds = new Rectangle();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            screenBounds = screenBounds.union(gd.getDefaultConfiguration().getBounds());
        }
    }

    private void loadPetImages() {
        try {
            loadFrames(idleFrames, "/images/idle/idle_", 2);
            loadFrames(happyFrames, "/images/happy/happy_", 10);
            loadFrames(walkRightFrames, "/images/walk_right/walk_right_", 8);
            loadFrames(walkLeftFrames, "/images/walk_left/walk_left_", 8);
            loadFrames(sleepFrames, "/images/sleep/sleep_", 9);
        } catch (Exception e) {
            e.printStackTrace();
            createPlaceholderImages();
        }
    }

    private void loadFrames(List<BufferedImage> frameList, String basePath, int count) throws Exception {
        for (int i = 1; i <= count; i++) {
            BufferedImage frame = loadImage(basePath + i + ".png");
            if (frame != null) {
                frameList.add(frame);
            }
        }
    }

    private BufferedImage loadImage(String path) throws Exception {
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource(path));

        MediaTracker tracker = new MediaTracker(new Component() {});
        tracker.addImage(image, 0);
        tracker.waitForAll();

        BufferedImage buffered = new BufferedImage(
                image.getWidth(null),
                image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB_PRE
        );

        Graphics2D g2d = buffered.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        return buffered;
    }

    private void initAnimation() {
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        animationTimer = new Timer(300, e -> {
            long startTime = System.nanoTime();

            updateAnimationState();
            repaint();


            long elapsed = (System.nanoTime() - startTime) / 1_000_000;
            if (elapsed < 300) {
                animationTimer.setDelay(300 - (int)elapsed);
            }
        });
        animationTimer.setCoalesce(true);
        animationTimer.start();
    }

    private void updateAnimationState() {
        switch (currentState) {
            case IDLE:
                currentFrame = (currentFrame + 1) % happyFrames.size();
                if (random.nextInt(500) < 2) {
                    lastState = PetState.IDLE;
                    currentState = PetState.SLEEPING;
                }
                break;

            case SLEEPING:
                currentFrame = (currentFrame + 1) % sleepFrames.size();
                if (random.nextInt(350) < 2) {
                    currentState = (lastState == PetState.IDLE) ? PetState.IDLE : PetState.WALKING;
                }
                break;

            case WALKING:
                currentFrame = isWalk ? (currentFrame + 1) % walkRightFrames.size() : (currentFrame + 1) % walkLeftFrames.size();
                if (random.nextInt(500) < 2) {
                    lastState = PetState.WALKING;
                    currentState = PetState.SLEEPING;
                } else {
                    moveWindow();
                }
                if (random.nextInt(500) < 2) {
                    isWalk = !isWalk;
                }
                break;
        }
    }
    private void moveWindow() {
        //正在拖拽不自动移动
        if (isDragging) return;

        Point currentPos = getLocation();
        int newX = currentPos.x;

        //边界检测自动转向
        if (newX <= screenBounds.x) { // 碰到左边界
            isWalk = true;
            newX = screenBounds.x;
        }
        else if (newX + getWidth() >= screenBounds.x + screenBounds.width) { //碰到右边界
            isWalk = false;
            newX = screenBounds.x + screenBounds.width - getWidth();
        }

        //计算新位置
        if (isWalk) {
            newX += moveSpeed;
        } else {
            newX -= moveSpeed;
        }

        // 应用新位置
        setLocation(newX, currentPos.y);

    }

    private void createPlaceholderImages() {
        for (int i = 0; i < 4; i++) {
            BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.dispose();

            idleFrames.add(img);
        }
    }


    @Override
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        BufferedImage currentImage = getCurrentFrame();
        if (currentImage != null) {
            g2d.drawImage(currentImage, 0, 0, null);
        }
    }

    private BufferedImage getCurrentFrame() {
        switch (currentState) {
            case IDLE: return happyFrames.get(currentFrame % happyFrames.size());
            case SLEEPING: return sleepFrames.get(currentFrame % sleepFrames.size());
            case WALKING: return isWalk ? walkRightFrames.get(currentFrame % walkRightFrames.size()) :
                    walkLeftFrames.get(currentFrame % walkLeftFrames.size());
            default: return null;
        }
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) return;

        // macOS 系统托盘特殊处理
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.awt.UIElement", "false");
            System.setProperty("apple.awt.brushMetalLook", "true");
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png"));

            PopupMenu popup = new PopupMenu();
            MenuItem exitItem = new MenuItem("exit");
            exitItem.addActionListener(e -> System.exit(0));
            popup.add(exitItem);

            TrayIcon trayIcon = new TrayIcon(image, "桌面宠物", popup);
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (Exception e) {
            System.err.println("无法创建系统托盘图标: " + e.getMessage());
        }
    }

    private void setupMouseListeners() {
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { dragOffset = e.getPoint(); isDragging = true; }
            @Override public void mouseReleased(MouseEvent e) { isDragging = false; }

            @Override public void mouseClicked(MouseEvent e) {
                switch (e.getClickCount()) {
                    case 1: toggleIdleSleep(); break;
                    case 2: toggleWalking(); break;
                    case 3: System.exit(0); break;
                }
                initAnimation();
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    Point newLocation = e.getLocationOnScreen();
                    setLocation(newLocation.x - dragOffset.x, newLocation.y - dragOffset.y);
                }
            }
        });
    }

    private void toggleIdleSleep() {
        currentState = (currentState == PetState.IDLE) ? PetState.SLEEPING :
                (currentState == PetState.SLEEPING) ? PetState.IDLE : PetState.IDLE;
    }

    private void toggleWalking() {
        isWalk = !isWalk;
        currentState = PetState.WALKING;
    }
}