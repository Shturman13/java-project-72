build:
	make -C app build

test:
	make -C app test

lint:
	make -C app lint

report:
	make -C app report

sonar:
	make -C app sonar

.PHONY: build test lint



