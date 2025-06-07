package com.hhhu;

import javax.swing.*;

public class DesktopPet {
    public static void main(String[] args) {
        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("java.awt.im.style", "none");
            System.setProperty("apple.awt.graphics.UseQuartz", "true");
        }
        SwingUtilities.invokeLater(() -> {
            PetFrame pet = new PetFrame();
            pet.setVisible(true);
            JOptionPane.showMessageDialog(pet,
                            "操作说明:\n" +
                            "拖拽移动宠物\n" +
                            "点击切换摇尾巴、睡觉\n" +
                            "双击行走\n"+
                            "三击退出",
                    "桌面宠物", JOptionPane.INFORMATION_MESSAGE);
        });
    }
}