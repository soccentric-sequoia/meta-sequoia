# Sequoia-OS

**Remote:** `git@github.com:SiliconCentric/Sequoia-OS.git`
**Parent project:** [Sequoia](../Sequoia.md) (target hardware: **Intel x86** SoM + platform-I/O south-bridge, reference board **LattePanda**.)
**Leaf type:** `<Project>-OS`
**Status:** REAL — actively built out

Yocto-based embedded Linux OS for the **Intel x86 / LattePanda** platform.
Built with `kas` + Docker (`genericx86-64` machine, EFI/GRUB boot), with A/B
OTA updates via `swupdate`.

**Board:** default `sequoia` (machine `genericx86-64`). Unlike other projects,
**Sequoia-OS is itself the `meta-sequoia` Yocto layer** — `conf/layer.conf`
lives at the repo root, and `kas/repos/meta-sequoia.yaml` points at `path: .`.

**Targets:** `make build`, `make image`, `make shell [board]`, `make swu`,
`make sdk`/`esdk`, `make flash`, `make deploy-*`, `make clean`/`clean-all`.

**Boot flow (Intel x86 / LattePanda):**
```
UEFI → GRUB (grub.cfg, A/B-aware) → Linux (Image + initrd) → systemd
     → Sequoia-HAL, services
```

**A/B OTA layout:** EFI GPT — `boot` (FAT32), `rootfs-a`, `rootfs-b`
(switched by swupdate via filesystem label), `data` (`/home`, persistent).
See `wic/sequoia-intel.wks`.

**Local layer:** this repo root **is** the layer; upstream layers are fetched
by kas into `layers/`. Legacy RPi5 recipes are retained for reference.

---
Part of the `Sequoia` project in the `project-managers` fleet. See
`docs/project-managers.md` for the full 9-project / 8-leaf hierarchy and shared
conventions.
