import json

def transform_style():
    with open('osm_bright.json', 'r', encoding='utf-8') as f:
        style = json.load(f)

    # Update Metadata
    style['name'] = "Circuit Style Map"
    
    # 1. Background
    # Find background layer
    for layer in style['layers']:
        if layer['type'] == 'background':
            if 'paint' not in layer: layer['paint'] = {}
            layer['paint']['background-color'] = '#f2f2f2'

    # 2. Landcover / Landuse (Generic Grayscale)
    # We will iterate and force grayscale on known land layers if they have color
    # Or just target specific ones if we want to be precise.
    # The user wants "landcover" and "landuse" to be present.
    # We will set a generic rule: if it's landuse/landcover, make it light gray.
    
    for layer in style['layers']:
        lid = layer['id']
        if 'landcover' in lid or 'landuse' in lid:
            if 'paint' in layer:
                if 'fill-color' in layer['paint']:
                    # Simplify to a single gray or a match expression that returns grays
                    # For simplicity and robustness, let's use a subtle gray
                    layer['paint']['fill-color'] = '#e0e0e0'
                    if 'fill-opacity' in layer['paint']:
                         layer['paint']['fill-opacity'] = 0.9

    # 3. Water (No Blue)
    for layer in style['layers']:
        if 'water' in layer['id'] and layer['type'] == 'fill':
             if 'paint' in layer:
                 layer['paint']['fill-color'] = '#dcdcdc' # Darker gray than background
        if 'waterway' in layer['id'] and layer['type'] == 'line':
             if 'paint' in layer:
                 layer['paint']['line-color'] = '#cccccc'

    # 4. Roads
    # We need to identify road layers. usually 'transportation' source-layer.
    # We need to separate casing and fill if they aren't already, or modify existing ones.
    # osm_bright usually has specific layers for different road classes.
    
    # User Requirements:
    # Casing: #b0b0b0
    # Fill: #ffffff or #f5f5f5
    # Widths: specific interpolations
    
    # We will iterate through layers and check for 'transportation' source-layer
    
    for layer in style['layers']:
        if layer.get('source-layer') == 'transportation':
            # Check class filters to apply specific widths
            is_motorway = False
            is_primary = False # trunk, primary, secondary, tertiary
            is_street = False # residential, service, etc
            
            # This is a heuristic, we might need to be more precise with filters
            # But usually osm_bright has distinct layers for these.
            
            # Let's look at the layer ID or filter to guess the type
            lid = layer['id']
            
            # Casing usually has 'casing' in ID or is drawn before fill with wider width
            # But osm_bright might use line-width and line-gap-width or just separate layers.
            # Let's assume separate layers or just force our style on what looks like them.
            
            # Actually, the user provided a sample of how they want it.
            # To ensure we don't break the existing structure, we should try to map existing layers to these styles.
            # OR, since the user said "Manter TODOS os layers essenciais... Apenas modificar paint...",
            # we should modify the existing layers in place.
            
            # Strategy:
            # 1. Identify if it's a Casing layer or Fill layer.
            #    - Casing layers usually are drawn first (lower index) or have 'casing' in ID.
            #    - osm_bright might not have explicit 'casing' layers for everything, it might use one layer with outline? 
            #      No, MapLibre lines don't have outline. They need a separate layer.
            #      If osm_bright doesn't have casing layers, we might need to add them or just style the lines.
            #      However, osm_bright DOES have casing layers (usually named `transportation-casing-*`).
            
            # Let's try to detect based on ID
            
            if 'casing' in lid:
                # Apply Casing Style
                if 'paint' not in layer: layer['paint'] = {}
                layer['paint']['line-color'] = '#b0b0b0'
                
                # Widths
                if 'motorway' in lid:
                    layer['paint']['line-width'] = {"stops": [[5, 1.0], [10, 2.0], [14, 3.5]]}
                elif any(x in lid for x in ['trunk', 'primary', 'secondary', 'tertiary']):
                    layer['paint']['line-width'] = {"stops": [[7, 0.8], [10, 1.8], [14, 3.2]]}
                else:
                    # Streets/others
                    layer['paint']['line-width'] = {"stops": [[10, 0.3], [13, 0.7], [16, 1.4]]} # Fallback
                    
            else:
                # Apply Fill Style
                if 'paint' not in layer: layer['paint'] = {}
                
                # Color
                if 'motorway' in lid:
                    layer['paint']['line-color'] = '#cccccc'
                    layer['paint']['line-width'] = {"stops": [[5, 0.8], [10, 1.6], [14, 3.0]]}
                elif any(x in lid for x in ['trunk', 'primary', 'secondary', 'tertiary']):
                    layer['paint']['line-color'] = '#e0e0e0'
                    layer['paint']['line-width'] = {"stops": [[7, 0.6], [10, 1.4], [14, 2.6]]}
                else:
                    # Residential/Street
                    layer['paint']['line-color'] = '#ffffff'
                    layer['paint']['line-width'] = {"stops": [[10, 0.3], [13, 0.7], [16, 1.4]]}

    # 5. Labels (transportation_name)
    for layer in style['layers']:
        if layer.get('source-layer') == 'transportation_name':
             if 'layout' not in layer: layer['layout'] = {}
             if 'paint' not in layer: layer['paint'] = {}
             
             # Minzoom
             layer['minzoom'] = 10
             
             # Text Size
             layer['layout']['text-size'] = {"stops": [[10, 10], [13, 13], [17, 17]]}
             
             # Halo
             layer['paint']['text-halo-color'] = '#ffffff'
             layer['paint']['text-halo-width'] = 1.4
             layer['paint']['text-color'] = '#334155'

    # 6. Place Labels
    for layer in style['layers']:
        if layer.get('source-layer') == 'place':
            if 'paint' not in layer: layer['paint'] = {}
            layer['paint']['text-halo-color'] = '#ffffff'
            
            # Ensure legibility
            if 'city' in layer['id']:
                 layer['paint']['text-halo-width'] = 1.6
                 layer['paint']['text-color'] = '#0f172a'
            else:
                 layer['paint']['text-halo-width'] = 1.2
                 layer['paint']['text-color'] = '#4b5563'

    # 7. Building
    for layer in style['layers']:
        if layer.get('source-layer') == 'building':
             if 'paint' not in layer: layer['paint'] = {}
             layer['paint']['fill-color'] = '#cccccc'
             if 'fill-outline-color' in layer['paint']:
                 layer['paint']['fill-outline-color'] = '#b3b3b3'
    
    # 8. Boundary
    for layer in style['layers']:
        if layer.get('source-layer') == 'boundary':
             if 'paint' not in layer: layer['paint'] = {}
             layer['paint']['line-color'] = '#9ca3af'

    with open('circuit_style.json', 'w', encoding='utf-8') as f:
        json.dump(style, f, indent=2)

if __name__ == "__main__":
    transform_style()
