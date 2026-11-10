import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class StudentMarksAnalysis {

    // Mapper Class: Parses CSV record and emits <RollNo - Name, SubjectMarks>
    public static class MarksMapper extends Mapper<LongWritable, Text, Text, IntWritable> {
        private final Text studentKey = new Text();
        private final IntWritable marksValue = new IntWritable();

        @Override
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String line = value.toString().trim();
            if (line.isEmpty()) {
                return;
            }

            String[] fields = line.split(",");
            if (fields.length == 4) {
                String rollNumber = fields[0].trim();
                String studentName = fields[1].trim();
                int subjectMarks = Integer.parseInt(fields[3].trim());

                studentKey.set(rollNumber + " - " + studentName);
                marksValue.set(subjectMarks);
                context.write(studentKey, marksValue);
            }
        }
    }

    // Reducer Class: Aggregates marks per student and calculates Average and Grade
    public static class MarksReducer extends Reducer<Text, IntWritable, Text, Text> {
        private final Text resultSummary = new Text();

        @Override
        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            int totalMarks = 0;
            int subjectCount = 0;

            for (IntWritable val : values) {
                totalMarks += val.get();
                subjectCount++;
            }

            double average = (subjectCount > 0) ? (double) totalMarks / subjectCount : 0.0;

            String grade;
            if (average >= 80.0) {
                grade = "A";
            } else if (average >= 60.0) {
                grade = "B";
            } else if (average >= 40.0) {
                grade = "C";
            } else {
                grade = "F";
            }

            String output = String.format("Total: %d | Avg: %.2f%% | Grade: %s",
                    totalMarks, average, grade);
            resultSummary.set(output);
            context.write(key, resultSummary);
        }
    }

    // Driver Method: Configures and submits the MapReduce job to YARN/Hadoop
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: StudentMarksAnalysis <HDFS input path> <HDFS output path>");
            System.exit(-1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Student Examination Marks Analysis");
        job.setJarByClass(StudentMarksAnalysis.class);

        job.setMapperClass(MarksMapper.class);
        job.setReducerClass(MarksReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
