
import json
import random
from datetime import datetime, timedelta

def generate_courses(num_courses=50):
    categories = ["Math", "Science", "Art", "History", "English", "Music", "Physical Education", "Technology"]
    course_types = ["ONE_TIME", "COURSE", "CLUB"]
    grade_ranges = ["1st-3rd", "4th-6th", "7th-8th", "9th-12th"]
    
    courses = []
    for i in range(1, num_courses + 1):
        min_age = random.randint(5, 15)
        course = {
            "id": i,
            "title": f"Course Title {i}",
            "description": f"This is a detailed description for course {i}. It covers various interesting topics.",
            "category": random.choice(categories),
            "type": random.choice(course_types),
            "gradeRange": random.choice(grade_ranges),
            "minAge": min_age,
            "maxAge": min_age + random.randint(1, 3),
            "price": round(random.uniform(20.0, 200.0), 2),
            "nextSessionDate": (datetime.now() + timedelta(days=random.randint(1, 60))).replace(microsecond=0).isoformat() + "Z"
        }
        courses.append(course)
        
    return courses

if __name__ == "__main__":
    courses_data = generate_courses(50)
    file_path = "src/main/resources/sample-courses.json"
    with open(file_path, "w") as f:
        json.dump(courses_data, f, indent=2)
    print(f"Successfully generated {len(courses_data)} courses in {file_path}")
