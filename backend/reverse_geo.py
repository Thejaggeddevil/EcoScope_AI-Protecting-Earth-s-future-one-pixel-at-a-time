import requests

def get_area_type(lat, lon):
    url = (
        "https://nominatim.openstreetmap.org/reverse?"
        f"format=json&lat={lat}&lon={lon}&zoom=10&addressdetails=1"
    )
    headers = {"User-Agent": "Mozilla/5.0 (compatible; AreaTypeBot/1.0)"}
    try:
        resp = requests.get(url, headers=headers, timeout=10)
        resp.raise_for_status()
        data = resp.json()
        category = data.get("category", "").lower()
        type_ = data.get("type", "").lower()
        display_name = data.get("display_name", "").lower()

        if "glacier" in display_name or "glacier" in type_:
            return "glacial"
        elif "city" in type_ or "urban" in display_name or "town" in type_:
            return "urban"
        else:
            return "rural"
    except Exception as e:
        print(f"Error in get_area_type: {e}")
        return "rural"
