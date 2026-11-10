import sys

def show(subject, marks):
    if not marks:
        return
    count = len(marks)
    avg = sum(marks) / count
    passed = sum(m >= 40 for m in marks)
    failed = count - passed
    print(f"{subject}\t{count}\t{avg:.2f}\t"
          f"{max(marks)}\t{min(marks)}\t{passed}\t{failed}")

current = None
marks_list = []

for line in sys.stdin:
    try:
        subject, value = line.rstrip("\n").split("\t", 1)
        mark = int(value)
    except ValueError:
        continue
    if subject != current:
        if current is not None:
            show(current, marks_list)
        current = subject
        marks_list = []
    marks_list.append(mark)

if current is not None:
    show(current, marks_list)
