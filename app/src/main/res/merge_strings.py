
import xml.etree.ElementTree as ET
import os

def merge_strings(base_path, target_path, output_path):
    base_tree = ET.parse(base_path)
    base_root = base_tree.getroot()
    
    target_strings = {}
    if os.path.exists(target_path):
        target_tree = ET.parse(target_path)
        target_root = target_tree.getroot()
        for string in target_root.findall('string'):
            target_strings[string.get('name')] = string.text

    # Create new root
    new_root = ET.Element('resources')
    
    for base_string in base_root.findall('string'):
        name = base_string.get('name')
        new_string = ET.SubElement(new_root, 'string')
        new_string.set('name', name)
        new_string.set('formatted', base_string.get('formatted', 'false'))
        
        if name in target_strings:
            new_string.text = target_strings[name]
        else:
            new_string.text = base_string.text
            
    # Pretty print
    from xml.dom import minidom
    xml_str = ET.tostring(new_root, encoding='utf-8')
    pretty_xml = minidom.parseString(xml_str).toprettyxml(indent="    ")
    
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(pretty_xml)

base = r'c:\Users\91700\Downloads\web to app\app\src\main\res\values\app_strings_ai.xml'
locales = ['values-zh', 'values-hi', 'values-ar']

for locale in locales:
    target = f'c:\\Users\\91700\\Downloads\\web to app\\app\\src\\main\\res\\{locale}\\app_strings_ai.xml'
    print(f"Merging {locale}...")
    merge_strings(base, target, target)
    print(f"Done {locale}")
