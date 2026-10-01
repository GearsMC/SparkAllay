/*
 * This file is part of spark.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.lucko.spark.allay;

import me.lucko.spark.common.SparkPlatform;
import org.allaymc.api.command.Command;
import org.allaymc.api.command.CommandResult;
import org.allaymc.api.command.CommandSender;
import org.allaymc.api.command.tree.CommandTree;

import java.util.List;
import java.util.Locale;

/**
 * Türkçe ve sadeleştirilmiş performans komutu: {@code /performans} (kısaca {@code /perf}).
 *
 * <p>Her alt komut, spark'ın İngilizce komutlarından birine hazır bayraklarla çevrilir;
 * böylece {@code --timeout}, {@code --thread} gibi bayrakları ezberlemek gerekmez. Spark'ın
 * kendi çıktıları ve web görüntüleyicisi İngilizce kalır. Gelişmiş kullanım için
 * {@code /spark} aynen duruyor.</p>
 */
public class AllayPerformanceCommand extends Command {
    private static final int DEFAULT_PROFILE_SECONDS = 120;
    private static final int MIN_PROFILE_SECONDS = 30;
    private static final int MAX_PROFILE_SECONDS = 1800;

    private final SparkPlatform platform;

    public AllayPerformanceCommand(SparkPlatform platform) {
        super("performans", "Sunucu performansını ölçer (spark'ın Türkçe kısayolu)", "spark.command");
        this.aliases.add("perf");
        this.platform = platform;
    }

    /**
     * Yalnızca istemcideki komut önerileri için; ayrıştırma {@link #execute} içinde yapılır.
     */
    @Override
    public void prepareCommandTree(CommandTree tree) {
        var root = tree.getRoot();
        root.key("yardim");
        root.key("durum");
        root.key("profil").intNum("saniye", DEFAULT_PROFILE_SECONDS).optional();
        root.key("durdur");
        root.key("iptal");
        root.key("bellek");
        root.key("gc");
        root.key("rapor");
        root.key("izle");
        root.key("ping").playerTarget("oyuncu").optional();
    }

    @Override
    public CommandResult execute(CommandSender sender, String[] args) {
        var spark = new AllayCommandSender(sender);
        var sub = args.length == 0 ? "yardim" : args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "durum" -> run(spark, "tps");
            case "profil" -> {
                int seconds = parseSeconds(sender, args);
                if (seconds < 0) {
                    return CommandResult.fail();
                }
                sender.sendMessage("§aProfil başladı: §f" + seconds + " sn§a sürecek. Bitince rapor linki gelecek."
                        + " Erken bitirmek için §f/performans durdur§a.");
                // Uyuyan/bekleyen thread'ler sayilmaz; rapor yalnizca gercekten CPU harcayan kodu gosterir.
                run(spark, "profiler", "start", "--timeout", String.valueOf(seconds), "--ignore-sleeping");
            }
            case "durdur" -> {
                sender.sendMessage("§eProfil durduruluyor, rapor hazırlanıyor...");
                run(spark, "profiler", "stop");
            }
            case "iptal" -> {
                sender.sendMessage("§eProfil rapor oluşturulmadan iptal ediliyor.");
                run(spark, "profiler", "cancel");
            }
            case "bellek" -> {
                sender.sendMessage("§eBellek özeti çıkarılıyor, rapor linki gelecek...");
                run(spark, "heapsummary");
            }
            case "gc" -> run(spark, "gc");
            case "rapor" -> {
                sender.sendMessage("§eSağlık raporu hazırlanıyor, rapor linki gelecek...");
                run(spark, "healthreport", "--upload");
            }
            case "izle" -> {
                sender.sendMessage("§eAna thread tick izleme açılıp kapatılır (tekrar yazınca kapanır).");
                run(spark, "tickmonitor");
            }
            case "ping" -> {
                if (args.length >= 2) {
                    run(spark, "ping", "--player", args[1]);
                } else {
                    run(spark, "ping");
                }
            }
            case "yardim", "help" -> sendHelp(sender);
            default -> {
                sender.sendMessage("§cBilinmeyen alt komut: §f" + args[0]);
                sendHelp(sender);
                return CommandResult.fail();
            }
        }
        return CommandResult.success(null);
    }

    private int parseSeconds(CommandSender sender, String[] args) {
        if (args.length < 2) {
            return DEFAULT_PROFILE_SECONDS;
        }
        try {
            int seconds = Integer.parseInt(args[1]);
            if (seconds < MIN_PROFILE_SECONDS || seconds > MAX_PROFILE_SECONDS) {
                sender.sendMessage("§cSüre " + MIN_PROFILE_SECONDS + " ile " + MAX_PROFILE_SECONDS
                        + " saniye arasında olmalı.");
                return -1;
            }
            return seconds;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cSüre sayı olmalı: §f" + args[1]);
            return -1;
        }
    }

    private void run(AllayCommandSender sender, String... sparkArgs) {
        this.platform.executeCommand(sender, sparkArgs);
    }

    private static void sendHelp(CommandSender sender) {
        for (var line : List.of(
                "§6--- Performans (spark) ---",
                "§e/performans durum §7- TPS, CPU ve bellek özeti",
                "§e/performans profil [saniye] §7- Tüm thread'leri profiller (varsayılan "
                        + DEFAULT_PROFILE_SECONDS + " sn, uyuyan thread'ler hariç), sonunda rapor linki verir",
                "§e/performans durdur §7- Profili erken bitirir ve linki verir",
                "§e/performans iptal §7- Profili rapor oluşturmadan iptal eder",
                "§e/performans bellek §7- Hangi sınıfın ne kadar bellek tuttuğu (link)",
                "§e/performans gc §7- Çöp toplayıcı (GC) istatistikleri",
                "§e/performans rapor §7- Genel sağlık raporu (link)",
                "§e/performans izle §7- Ana thread tick izleme aç/kapa",
                "§e/performans ping [oyuncu] §7- Oyuncu pingleri",
                "§7Not: Allay'de dünyalar kendi thread'lerinde döner; TPS ana thread'i gösterir.",
                "§7Dünya kaynaklı lag için §e/performans profil§7 kullan.",
                "§7Gelişmiş kullanım: §e/spark"
        )) {
            sender.sendMessage(line);
        }
    }
}
