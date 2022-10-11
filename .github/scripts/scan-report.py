import os
import pathlib
import sys


def write_report(file, report):
    os.makedirs(pathlib.Path(file).parent, exist_ok=True)
    print(f"Writing report to {file}...")
    with open(file, 'w') as f:
        f.write(report)
    print(f"> Wrote report to {file}.")


def read_scan(path):
    with open(path, 'r') as f:
        lines = f.readlines()
    lines = [line.strip() for line in lines]
    return [line for line in lines if len(line) > 0]


def create_report(scan_paths):
    print(f"Creating report for {len(scan_paths)} scans...")
    result = []
    scans = [read_scan(path) for path in scan_paths]
    if len(scans) > 0:
        result.append(scans[0][0])
    for scan in scans:
        result.extend(scan[1:])
    print(f"> Created report.")
    return "\n".join(result)


def main():
    input_dir = sys.argv[1]
    output_file = sys.argv[2]
    scan_paths = [os.path.join(input_dir, p) for p in os.listdir(input_dir) if p.endswith(".csv")]
    write_report(output_file, create_report(scan_paths))


if __name__ == "__main__":
    main()
