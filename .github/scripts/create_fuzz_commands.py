import csv
import sys


def read_csv(path):
    with open(path, 'r') as f:
        reader = csv.DictReader(f)
        return [row for row in reader]


def read_lines(path):
    with open(path, 'r') as f:
        return f.readlines()


def create_command(row):
    duration = "PT1M"
    quiet = False
    module = ":".join(row["project_id"].split(':')[0:2])
    return " ".join([
        "mvn",
        "-pl",
        module,
        "-am",
        "integration-test",
        f"-Dpomelo.task=fuzz",
        f"""-Dpomelo.project={row["project_id"]}""",
        f"""-Dpomelo.plugin={row['plugin']}""",
        f"""-Dpomelo.execution={row['execution_id']}""",
        f"""-Dpomelo.testClass={row['test_class_name']}""",
        f"""-Dpomelo.testMethod={row['test_method_name']}""",
        f"""-Dpomelo.duration={duration}""",
        f"""-Dpomelo.jacocoFormats=CSV"""
        f"""-Dpomelo.fuzz.quiet""" if quiet else "",
    ]).strip()


def is_invalid(row):
    return (row['isolated_result'] == 'PASSED' or row['isolated_result'] == 'TIMED_OUT') and row[
        'generators_status'] == 'PRESENT'


def main():
    rows = [row for row in read_csv(sys.argv[1]) if is_invalid(row)]
    for row in rows:
        print(create_command(row))


if __name__ == "__main__":
    main()
