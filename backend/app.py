from flask import Flask, request, jsonify, send_from_directory
import os
from download_img import download_image
from reverse_geo import get_area_type

app = Flask(__name__, static_folder='static')

@app.route('/analyze', methods=['POST'])
def analyze():
    data = request.get_json()
    if not data:
        return jsonify({"error": "Invalid or missing JSON"}), 400
    lat = data.get('lat')
    lon = data.get('lon')
    before = data.get('before')
    after = data.get('after')

    # Area type detection
    area_type = get_area_type(lat, lon)

    # Generate before and after images
    static_dir = os.path.join(os.path.dirname(__file__), 'static')
    os.makedirs(static_dir, exist_ok=True)
    before_path = os.path.join(static_dir, 'before.png')
    after_path = os.path.join(static_dir, 'after.png')
    download_image(lat, lon, before, before_path)
    download_image(lat, lon, after, after_path)

    # --- AI Model Placeholder ---
    # TODO: Integrate AI model here when ready
    # Example: results = run_ai_model(area_type, before_path, after_path)
    # For now, use dummy values below

    return jsonify({
        "area_type": area_type,
        "lake_change": "15.4%" if area_type == "glacial" else None,
        "road_added_km": 2.4 if area_type == "urban" else None,
        "drainage_change": "Moderate" if area_type == "urban" else None,
        "before_url": "http://localhost:5000/static/before.png",
        "after_url": "http://localhost:5000/static/after.png",
        "change_map_url": "http://localhost:5000/static/change_map.png"
    })

@app.route('/static/<path:filename>')
def serve_static(filename):
    return send_from_directory('static', filename)

if __name__ == '__main__':
    app.run(debug=True)
