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
    private enum PetState{
        IDLE,SLEEPING,WALKING
    }
    private PetState currentState = PetState.IDLE;
    private PetState lastState = PetState.IDLE;
    private Timer animationTimer;
    private Random random = new Random();
    private Rectangle screenBounds;
    private int currentFrame = 0;//动画帧索引
    private int moveSpeed = 3;
    private final List<BufferedImage> idleFrames = new ArrayList<>();
    private final List<BufferedImage> happyFrames = new ArrayList<>();
    private final List<BufferedImage> walkRightFrames = new ArrayList<>();
    private final List<BufferedImage> walkLeftFrames = new ArrayList<>();
    private final List<BufferedImage> sleepFrames = new ArrayList<>();

    private Point dragOffset = new Point();
    private boolean isDragging = false;
    private boolean isWalk = false;//false 向右 true向左




    public PetFrame() {
        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0));
        setAlwaysOnTop(true);
        setSize(100, 100);

        //获取屏幕边缘
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        screenBounds = new Rectangle();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            screenBounds = screenBounds.union(gd.getDefaultConfiguration().getBounds());
        }


        //加载图像资源
        loadPetImages();

        // 初始化动画定时器
        initAnimation();




        //鼠标监听器
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragOffset = e.getPoint();
                isDragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 1){
                    if(currentState == PetState.IDLE){
                        currentState = PetState.SLEEPING;
                    }else if(currentState == PetState.SLEEPING){
                        currentState = PetState.IDLE;
                    }else{
                        currentState = PetState.IDLE;
                    }
                    initAnimation();
                } else if(e.getClickCount() == 2){
                    isWalk = !isWalk;
                    currentState = PetState.WALKING;
                    initAnimation();
                }
                if (e.getClickCount() == 3) {
                    System.exit(0);
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isDragging) {
                    Point newLocation = e.getLocationOnScreen();
                    setLocation(newLocation.x - dragOffset.x, newLocation.y - dragOffset.y);
                }
            }
        });

        setupSystemTray();
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


    private void loadPetImages() {
        try {
            //加载空闲状态动画帧
            for (int i = 1; i <= 2; i++) {
                BufferedImage frame = ImageIO.read(getClass().getResource("/images/idle/idle_" + i + ".png"));
                idleFrames.add(frame);
            }

            //加载开心状态动画帧
            for (int i = 1; i <= 10; i++) {
               BufferedImage frame = ImageIO.read(getClass().getResource("/images/happy/happy_" + i + ".png"));
//                BufferedImage frame = ImageIO.read(getClass().getResource("/images/walk_right/walk_right_" + i + ".png"));
                happyFrames.add(frame);
            }
            //加载向右行走动画帧
            for (int i = 1; i <= 8; i++) {
                BufferedImage frame = ImageIO.read(getClass().getResource("/images/walk_right/walk_right_" + i + ".png"));
                walkRightFrames.add(frame);
            }

            //加载向左行走动画帧
            for (int i = 1; i <= 8; i++) {
                BufferedImage frame = ImageIO.read(getClass().getResource("/images/walk_left/walk_left_" + i + ".png"));
                walkLeftFrames.add(frame);
            }
            for (int i = 1; i <= 9; i++) {
                BufferedImage frame = ImageIO.read(getClass().getResource("/images/sleep/sleep_" + i + ".png"));
                sleepFrames.add(frame);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //加载失败，创建占位图像
            createPlaceholderImages();
        }
    }

    //初始化动画定时器
    private void initAnimation() {
        //停止定时器
        if (animationTimer != null && animationTimer.isRunning()) {
            animationTimer.stop();
        }

        animationTimer = new Timer(300, e -> {
            switch (currentState) {
                case IDLE:
                    currentFrame = (currentFrame + 1) % happyFrames.size();
                    //随机触发睡觉
                    if (random.nextInt(500)<2) {
                    lastState = PetState.IDLE;
                    currentState = PetState.SLEEPING;
                }

                    break;

                case SLEEPING:

                    currentFrame = (currentFrame + 1) % sleepFrames.size();
                    //随机结束睡觉
                    if (random.nextInt(350)<2) {
                        if(lastState == PetState.IDLE){
                            currentState = PetState.IDLE;
                        }else{
                            currentState = PetState.WALKING;
                        }

                    }
                    break;

                case WALKING:
                    currentFrame = isWalk ?  (currentFrame + 1) % walkRightFrames.size() : (currentFrame + 1) % walkLeftFrames.size();
                    //随机触发睡觉
                    if (random.nextInt(500)<2) {
                        lastState = PetState.WALKING;
                        currentState = PetState.SLEEPING;
                    } else {
                        moveWindow();
                    }

                    //每帧0.4%的概率切换方向
                    if(random.nextInt(500)<2){
                        isWalk = !isWalk;
                    }
                    break;
            }

            repaint();
        });

        animationTimer.start();
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

        //绘制当前帧
        BufferedImage currentImage = null;
//        if (Happy && isHappy && !happyFrames.isEmpty()) {
//            currentImage = happyFrames.get(currentFrame % happyFrames.size());
//        }else if(Happy && !isHappy && !idleFrames.isEmpty()){
//            currentImage = sleepFrames.get(currentFrame % sleepFrames.size());
//        }else if(!Happy && isWalk && !walkRightFrames.isEmpty()){
//            currentImage = walkRightFrames.get(currentFrame % walkRightFrames.size());
//        }else if(!Happy && !isWalk && !walkLeftFrames.isEmpty()){
//            currentImage = walkLeftFrames.get(currentFrame % walkLeftFrames.size());
//        }else{
//            currentImage = idleFrames.get(currentFrame % idleFrames.size());
//        }

        switch (currentState) {
            case IDLE:
                currentImage = happyFrames.get(currentFrame % happyFrames.size());
                break;
            case SLEEPING:
                currentImage = sleepFrames.get(currentFrame % sleepFrames.size());
                break;
            case WALKING:
                currentImage = isWalk ?  walkRightFrames.get(currentFrame % walkRightFrames.size()) : walkLeftFrames.get(currentFrame % walkLeftFrames.size());
                break;
        }


        g2d.drawImage(currentImage, 0, 0, null);
    }

    private void setupSystemTray() {
        if (!SystemTray.isSupported()) return;
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.awt.UIElement", "true");
        }

        SystemTray tray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().getImage(getClass().getResource("/images/icon.png"));

        PopupMenu popup = new PopupMenu();
        MenuItem exitItem = new MenuItem("exit");
        exitItem.addActionListener(e -> System.exit(0));
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(image, "桌面宠物", popup);
        trayIcon.setImageAutoSize(true);

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}