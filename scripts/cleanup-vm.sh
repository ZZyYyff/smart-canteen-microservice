#!/bin/bash
# =============================================
# 磁盘清理脚本 - 在 Ubuntu VM 里执行
# =============================================

echo "=== 1. 当前磁盘使用情况 ==="
df -h /

echo ""
echo "=== 2. 清理 systemd 日志 ==="
sudo journalctl --vacuum-size=100M

echo ""
echo "=== 3. 清理 apt 缓存 ==="
sudo apt clean
sudo apt autoremove --purge -y

echo ""
echo "=== 4. 清理 Docker 残留 ==="
sudo docker system prune -a -f 2>/dev/null || echo "Docker 未运行或无可清理"

echo ""
echo "=== 5. 清理 /tmp 下的大文件 ==="
du -sh /tmp/* 2>/dev/null | sort -rh | head -10
# 安全删除超过 7 天的 /tmp 文件
find /tmp -type f -mtime +7 -delete 2>/dev/null

echo ""
echo "=== 6. 清理 /var/log 日志文件 ==="
sudo du -sh /var/log/* 2>/dev/null | sort -rh | head -10
sudo find /var/log -type f -name "*.log" -mtime +7 -delete 2>/dev/null
sudo find /var/log -type f -name "*.gz" -delete 2>/dev/null

echo ""
echo "=== 7. 清理 snap 旧版本（如果有） ==="
which snap 2>/dev/null && sudo snap list --all 2>/dev/null | awk '/disabled/{print $1, $3}' | while read snapname revision; do sudo snap remove "$snapname" --revision="$revision" 2>/dev/null; done

echo ""
echo "=== 8. 清理 K3S containerd 中无用的镜像（保留 smart-canteen） ==="
sudo k3s ctr images ls 2>/dev/null | grep -v smart-canteen | tail -n +2 | awk '{print $1}' | xargs -r sudo k3s ctr images rm 2>/dev/null

echo ""
echo "=== 9. 清理后磁盘情况 ==="
df -h /
echo ""
echo "磁盘清理完成！"
