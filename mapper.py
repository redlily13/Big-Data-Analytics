import csv
import sys

reader = csv.reader(sys.stdin)
for row in reader:
    if not row or row[0] == "StudentID":
        continue
    try:
        student_id, name, subject, marks = row
        marks = int(marks)
        if not 0 <= marks <= 100:
            continue
        print(f"{subject}\t{marks}")
    except (ValueError, IndexError):
        continue
