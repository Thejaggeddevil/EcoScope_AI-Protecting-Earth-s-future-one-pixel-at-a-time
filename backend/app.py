from flask import Flask, request, jsonify, send_from_directory
from download_img import download_image
from reverse_geo import get_area_type
from PIL import Image, ImageChops
import os, uuid, logging
from typing import Any

app = Flask(__name__)
BASE_DIR   = os.path.dirname(__file__)
STATIC_DIR = os.path.join(BASE_DIR, "static")
os.makedirs(STATIC_DIR, exist_ok=True)

logging.basicConfig(level=logging.INFO)

@app.route("/analyze", methods=["POST"])
def analyze():
    data: dict[str, Any] = request.get_json(silent=True) or {}

    lat_raw: Any = data.get("lat")
    lon_raw: Any = data.get("lon")
    date_before_raw: Any = data.get("before")
    date_after_raw: Any = data.get("after")

    if lat_raw is None or lon_raw is None or date_before_raw is None or date_after_raw is None:
        return jsonify({"error": "Missing one or more required parameters"}), 400

    try:
        lat = float(lat_raw)
        lon = float(lon_raw)
        date_before = str(date_before_raw)
        date_after = str(date_after_raw)
    except (TypeError, ValueError):
        return jsonify({"error": "Invalid parameter types"}), 400

    uid = uuid.uuid4().hex

    before_tif = os.path.join(STATIC_DIR, f"{uid}_before.tif")
    after_tif  = os.path.join(STATIC_DIR, f"{uid}_after.tif")
    before_png = os.path.join(STATIC_DIR, f"{uid}_before.png")
    after_png  = os.path.join(STATIC_DIR, f"{uid}_after.png")
    diff_png   = os.path.join(STATIC_DIR, f"{uid}_change_map.png")

    try:
        # 1 Download Sentinel‑2 tiles
        download_image(lat, lon, date_before, before_tif)
        download_image(lat, lon, date_after,  after_tif)

        # 2 Convert to PNG
        Image.open(before_tif).save(before_png)
        Image.open(after_tif).save(after_png)

        # 3 Compute simple absolute‑difference change map
        diff = ImageChops.difference(Image.open(after_png), Image.open(before_png))
        diff.save(diff_png)

        # 4 Context label
        area_type = get_area_type(lat, lon)

        return jsonify(
            {
                "before_url":     f"/static/{uid}_before.png",
                "after_url":      f"/static/{uid}_after.png",
                "change_map_url": f"/static/{uid}_change_map.png",
                "area_type":      area_type,
            }
        )

    except Exception as e:
        app.logger.exception("Analyze failed")
        return jsonify({"error": f"Image processing failed: {e}"}), 500


@app.route("/static/<path:filename>")
def serve_static(filename: str):
    return send_from_directory(STATIC_DIR, filename)


if __name__ == "__main__":
    app.run(debug=os.getenv("FLASK_DEBUG") == "1", host="0.0.0.0")
