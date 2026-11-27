# Changelog

## [Unreleased]

### Added — build orchestration + docs

- **`Makefile`** — full build orchestration (`image`, `build`, `rebuild-image`,
  `sdk`, `esdk`, `shell`, `swu`, `flash`, `deploy-*`, `clean*`, `completion`),
  modeled on the shared project-managers template.
- **`docker/`** — `yocto/`, `sdk/`, `esdk/` Docker build containers
  (`sequoia-kas`, `sequoia-sdk`, `sequoia-esdk`).
- **`scripts/generate-swu-keys.sh`** — RSA-4096 keypair generator.
- **`scripts/kas-container`** — wrapper around the root `kas-container`.
- **`recipes-bsp/sequoia-grub-cfg`** — A/B-aware GRUB config staged for the
  EFI wic plugin (referenced by `kas/common-sequoia.yaml`).
- **`recipes-swupdate/sequoia-swupdate-config/files/swupdate-public.pem`** —
  public key placeholder + install path.
- **`sequoia-make-completion.bash`** — bash tab-completion.
- **`CLAUDE.md`**, **`REPO_MAP.md`**, **`CHANGELOG.md`** — project docs.
- **`.gitignore`** — tracks source, ignores build/layers/keys.

### Fixed

- `kas/repos/meta-sequoia.yaml` now uses `path: .` — Sequoia-OS **is** the
  `meta-sequoia` layer (root `conf/layer.conf`), so kas mounts the repo root
  as the layer rather than a nonexistent `meta/meta-sequoia`.

### Notes

- Sequoia-OS is architecturally a **single Yocto layer** at the repo root
  (not a nested `meta/meta-sequoia`), matching the project_specs.md
  description. It retains legacy Raspberry Pi 5 recipes (`rpi5-*`) for
  reference; the active image path uses the Sequoia recipes and the
  `genericx86-64` EFI/GRUB machine.
