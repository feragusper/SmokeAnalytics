import xml.etree.ElementTree as ET
import glob

# Analyze ALL modules - show classes being counted
files = sorted(glob.glob('**/build/smoke-analytics-report/result.xml', recursive=True))
for f in files:
    module = f.split('/build/')[0]
    tree = ET.parse(f)
    root = tree.getroot()

    # Get module total
    for counter in root.findall('counter'):
        if counter.get('type') == 'INSTRUCTION':
            missed = int(counter.get('missed', 0))
            covered = int(counter.get('covered', 0))
            total = missed + covered
            if total > 0:
                pct = covered * 100.0 / total
                if pct < 30:  # Only show low-coverage modules
                    print(f'\n=== {module} ({pct:.1f}%, {total} instr) ===')
                    for pkg in root.findall('.//package'):
                        for cls in pkg.findall('class'):
                            name = cls.get('name', '')
                            sf = cls.get('sourcefilename', '')
                            for c in cls.findall('counter'):
                                if c.get('type') == 'INSTRUCTION':
                                    m = int(c.get('missed', 0))
                                    cv = int(c.get('covered', 0))
                                    t = m + cv
                                    if t > 30:
                                        short = name.split('/')[-1]
                                        print(f'  {t:5} instr  {cv:5} cov  {short} ({sf})')
                                    break
            break

